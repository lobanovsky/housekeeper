package ru.housekeeper.service

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.AreaTypeEnum
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.access.AccessRequest
import ru.housekeeper.model.dto.access.Person
import ru.housekeeper.model.dto.access.Phone
import ru.housekeeper.model.dto.access.Room
import ru.housekeeper.model.entity.access.AccessInfo
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.repository.payment.IncomingPaymentRepository
import ru.housekeeper.repository.room.RoomRepository
import ru.housekeeper.service.access.AccessService
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.time.LocalDate

@Service
class RepairService(
    private val paymentService: PaymentService,
    private val paymentRepository: IncomingPaymentRepository,
    private val accessService: AccessService,
    private val roomRepository: RoomRepository,
    private val roomService: RoomService,

    ) {

    @Transactional
    fun findAndRemoveDuplicates() {
        val incomingPayments = paymentService.findAllIncomingPaymentsWithFilter(0, 10000, IncomingPaymentsFilter())
        val duplicatePayments = findIds(incomingPayments.content)
        logger().info("Remove all duplicate payments with ids = $duplicatePayments")
        paymentRepository.deleteByIds(duplicatePayments)
    }

    //find duplicate payments by uuid: date without time + docNumber + sum
    private fun findIds(payments: List<IncomingPayment>): List<Long> {
        val uuids = mutableSetOf<Pair<Long, String>>()
        payments.forEach {
            uuids.add(Pair(it.id ?: 0, "${it.date.toLocalDate()} ${it.docNumber} ${it.sum}"))
        }
        val groupByUUID = uuids.groupBy { it.second }
        val countOfDuplicates = payments.size - groupByUUID.size
        if (countOfDuplicates == 0) {
            logger().info("No duplicates found")
            return emptyList()
        }
        logger().info("IncomingPayments size = ${payments.size}, Unique UUIDs size = ${groupByUUID.size}, Duplicate UUIDs size = $countOfDuplicates")

        //show grouped incoming payments where values size more than one
        val groupById = payments.groupBy { it.id }
        val idsForRemove = mutableListOf<Long?>()
        var i = 1
        groupByUUID.filter { it.value.size > 1 }.forEach { it ->
            //show id, uuid, date, doc_number, sum, source for values
            logger().info("$i-------------------------------------------------")
            it.value.forEach {
                val payment = groupById[it.first]?.get(0)
                logger().info("id=${payment?.id}, account = ${payment?.account} uuid=${payment?.uuid}, date=${payment?.date}, doc_number=${payment?.docNumber}, sum=${payment?.sum}, source=${payment?.source}")
            }
            i++
            //get all values except first
            it.value.drop(1).forEach { idsForRemove.add(it.first) }
        }
        return idsForRemove.filterNotNull()
    }

    @Transactional
    fun updateUUID() {
        val incomingPayments = paymentService.findAllIncomingPaymentsWithFilter(0, 10000, IncomingPaymentsFilter())
        incomingPayments.content.forEach {
            it.uuid = "${it.date.toLocalDate()} ${it.docNumber} ${it.sum}"
        }
        logger().info("Update UUID for incoming payments, size = ${incomingPayments.content.size}")
    }

    fun getSumOfPayments(
        startDate: LocalDate, endDate:
        LocalDate, toAccounts: List<String>
    ): BigDecimal {
        val incomingPayments = paymentService.findAllIncomingPaymentsWithFilter(
            0, 10000, IncomingPaymentsFilter(
                startDate = startDate,
                endDate = endDate,
                toAccounts = toAccounts
            )
        )
        return incomingPayments.content
            .filterNot { it.purpose.contains("Возврат депозита по договору") }
            .map { it.sum }
            .fold(BigDecimal.ZERO, BigDecimal::add)
    }

    fun initAccessInfo(file: MultipartFile): Int {
        val workbook = WorkbookFactory.create(file.inputStream)
        val gateSheet = workbook.getSheet("gate")
        val parkingSheet = workbook.getSheet("parking")

        var access = mutableListOf<AccessInfo>()
        val contacts = mutableListOf<Contact>()

        var gateCount = 0
        for (i in 15..gateSheet.lastRowNum) {
            val row = gateSheet.getRow(i)
            val tenant = row.getCell(2).stringCellValue.trim() == "1"
            val label = row.getCell(4).stringCellValue.trim()
            val flat = row.getCell(5).stringCellValue
            val phone = row.getCell(6).stringCellValue

            if (phone.isNotBlank()) {
                gateCount++
                contacts.add(makePhone(flat, phone, label.ifBlank { null }, tenant, RoomTypeEnum.FLAT))
            }
        }

        var parkingCount = 0
        for (i in 5..parkingSheet.lastRowNum) {
            val row = parkingSheet.getRow(i)
            val tenant = row.getCell(2).stringCellValue.trim() == "1"
            val label = row.getCell(4).stringCellValue.trim()
            val flat = row.getCell(5).stringCellValue
            val phone = row.getCell(6).stringCellValue

            if (phone.isNotBlank()) {
                parkingCount++
                contacts.add(makePhone(flat, phone, label.ifBlank { null }, tenant, RoomTypeEnum.GARAGE))
            }
        }
        logger().info("Gate count = $gateCount, Parking count = $parkingCount")

        val groupingByPhone = contacts.groupBy { it.phone }
        println("Phones count = ${contacts.size}, Unique phones count = ${groupingByPhone.size}")

        var count = 0
        for (contact in groupingByPhone) {
            val areas = getAreas(contact.value.map { it.type }.toSet())
            val person = Person(
                phones = getPhones(contact.value),
                rooms = getRooms(contact.value),
                tenant = contact.value[0].tenant
            )
            val accesses = accessService.createAccessToArea(AccessRequest(areas, person))
            count += accesses.size
        }
        return count
    }

    private fun getPhones(contacts: List<Contact>): Set<Phone> {
        val maxLengthLabel = contacts.map { it.label }.maxBy { it?.length ?: 0 }
        return setOf(Phone(contacts[0].phone, maxLengthLabel))
    }


    private fun makePhone(flat: String, phone: String, label: String?, tenant: Boolean, type: RoomTypeEnum): Contact {
        val roomNumber = flat.split("-")[0]
        return Contact(roomNumber, phone, label, tenant, type)
    }

    private fun getAreas(types: Set<RoomTypeEnum>): Set<Long> {
        val areas = mutableSetOf<Long>()
        types.forEach {
            when (it) {
                RoomTypeEnum.FLAT -> areas.add(AreaTypeEnum.YARD_AREA.ordinal.toLong() + 1)
                RoomTypeEnum.GARAGE -> areas.add(AreaTypeEnum.UNDERGROUND_PARKING_AREA.ordinal.toLong() + 1)
                else -> {
                }
            }
        }
        return areas
    }

    private fun getRooms(contacts: List<Contact>): Set<Room> {
        val rooms = mutableSetOf<Room>()
        val groupByRoom = contacts.groupBy { it.type }
        for (type in groupByRoom) {
            val roomNumbers = type.value.map { it.roomNumber }.toSet()
            val roomsByType = roomRepository.findByRoomNumbersAndType(roomNumbers, type.key)
            val buildingId = roomsByType[0].building
            val roomIds: List<Long> = roomsByType.map { it.id!! }
            rooms.add(Room(buildingId, roomIds.toSet()))
        }
        return rooms
    }

    data class Contact(
        val roomNumber: String,
        val phone: String,
        val label: String? = null,
        val tenant: Boolean,
        val type: RoomTypeEnum
    )
}