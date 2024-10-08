package ru.housekeeper.service

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.AccessBlockReasonEnum
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.access.AccessRequest
import ru.housekeeper.model.dto.access.AreaRequest
import ru.housekeeper.model.dto.access.CarRequest
import ru.housekeeper.model.dto.access.CreateAccessRequest
import ru.housekeeper.model.dto.access.toArea
import ru.housekeeper.model.dto.access.toCar
import ru.housekeeper.model.entity.OwnerEntity
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.repository.payment.IncomingPaymentRepository
import ru.housekeeper.service.access.AccessService
import ru.housekeeper.service.gate.LogEntryService
import ru.housekeeper.utils.isValidRussianCarNumber
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.replaceLatinToCyrillic
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.collections.mutableSetOf

@Service
class RepairService(
    private val paymentService: PaymentService,
    private val paymentRepository: IncomingPaymentRepository,
    private val roomService: RoomService,
    private val ownerService: OwnerService,
    private val logEntryService: LogEntryService,
    private val accessService: AccessService,
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

    private fun findOwnersByRoom(roomNumber: String, buildingId: Long, roomType: RoomTypeEnum): List<OwnerEntity> {
        val room = roomService.findByNumberAndBuildingIdAndType(roomNumber, buildingId, roomType)
        return room?.id?.let { ownerService.findByRoomId(it) } ?: emptyList()
    }

    fun initYard(file: MultipartFile): Int {
        val workbook = WorkbookFactory.create(file.inputStream)
        val yardSheet = workbook.getSheet("yard - №\tBlock\tАренда\tfirst nam")
        if (yardSheet == null) {
            throw IllegalArgumentException("Yard sheet not found")
        }

        val buildingId = 1L
        val areaId = 1L

        val rows = accessRowParser(yardSheet, 15)
        var count = 0
        for (row in rows) {
            val owners = findOwnersByRoom(row.roomNumber, buildingId, RoomTypeEnum.FLAT)
            if (owners.isEmpty()) continue
            val ownerId = owners.first().id!!

            val accessResponses = createAccess(ownerId, areaId, row)

            count += accessResponses.size
        }
        logger().info("Created accesses count = $count")
        return count
    }

    private fun createAccess(ownerId: Long, areaId: Long, row: AccessRow) = accessService.create(
        request = CreateAccessRequest(
            ownerId = ownerId,
            accesses = setOf(AccessRequest(
                areas = if (areaId == 2L) listOf(AreaRequest(areaId, places = setOf(row.roomNumber))) else listOf(AreaRequest(areaId)),
                phoneNumber = row.phone,
                contactLabel = row.label,
                tenant = row.tenant,
                cars = row.carNumber?.let { setOf(CarRequest(it, row.carDescription)) }
            )),
        ),
        active = row.active
    )

    fun initGarage(file: MultipartFile): Int {
        val workbook = WorkbookFactory.create(file.inputStream)
        val parkingSheet = workbook.getSheet("parkning - Паркинг")
        if (parkingSheet == null) {
            throw IllegalArgumentException("Parking sheet not found")
        }

        val buildingId = 2L
        val areaId = 2L

        val rows = accessRowParser(parkingSheet, 5)

        var blockedCount = 0
        var newCount = 0
        var updatedCount = 0

        for (row in rows) {
            val owners = findOwnersByRoom(row.roomNumber, buildingId, RoomTypeEnum.GARAGE)
            if (owners.isEmpty()) continue
            val ownerId = owners.first().id!!

            //добавить новый неактивный
            if (row.active == false) {
                createAccess(ownerId, areaId, row)
                blockedCount++
                continue
            }

            val existAccess = accessService.findByOwnerIdAndPhone(ownerId, row.phone)

            //добавить новый
            if (existAccess == null) {
                createAccess(ownerId, areaId, row)
                newCount++
                continue
            }

            //обновляем
            if (row.phone == existAccess.phoneNumber) {
                //добавить доступ в гараж на определенное место
                existAccess.areas.add(AreaRequest(areaId, setOf(row.roomNumber)).toArea())
                //добавить авто
                row.carNumber?.let { CarRequest(it, row.carDescription) }?.let { existAccess.cars?.add(it.toCar()) }
                updatedCount++
            }
        }
        logger().info("Blocked count = $blockedCount, New count = $newCount, Updated count = $updatedCount")
        return blockedCount + newCount + updatedCount
    }


    private fun accessRowParser(
        gateSheet: Sheet,
        skipFirstRows: Int,
    ): MutableList<AccessRow> {
        val accessRows = mutableListOf<AccessRow>()
        var count = 0
        for (i in skipFirstRows..gateSheet.lastRowNum) {
            val row = gateSheet.getRow(i)
            val blocked = row.getCell(1).stringCellValue.trim() == "1"
            val tenant = row.getCell(2).stringCellValue.trim() == "1"
            val label = row.getCell(4).stringCellValue.trim()
            val flat = row.getCell(5).stringCellValue
            val phone = row.getCell(6).stringCellValue.trim()
            val carNumber = row.getCell(7).stringCellValue.trim().replaceLatinToCyrillic()
            val carDescription = row.getCell(8).stringCellValue.trim()

            if (carNumber.isNotBlank()) {
                if (!isValidRussianCarNumber(carNumber)) {
                    logger().warn("before = $carNumber, after = $carNumber: Invalid car number = $carNumber")
                }
            }
            if (phone.isBlank() && carNumber.isNotBlank()) {
                logger().warn("Car number = $carNumber, but phone is empty, for flat = $flat, label = $label")
            }

            if (phone.isBlank()) continue
            accessRows.add(
                makeAccessRow(
                    flat,
                    phone,
                    label.ifBlank { null },
                    tenant,
                    active = !blocked,
                    carNumber.ifBlank { null },
                    carDescription.ifBlank { null })
            )
            count++
        }
        logger().info("Sheet row count [${gateSheet.sheetName}] = $count")
        return accessRows
    }

    private fun makeAccessRow(
        flat: String,
        phone: String,
        label: String?,
        tenant: Boolean,
        active: Boolean,
        carNumber: String?,
        carDescription: String?
    ): AccessRow {
        val roomNumber = flat.split("-")[0]
        return AccessRow(roomNumber, phone, label, tenant, active, carNumber, carDescription)
    }

    data class AccessRow(
        val roomNumber: String,
        val phone: String,
        val label: String? = null,
        val tenant: Boolean,
        val active: Boolean,
        val carNumber: String? = null,
        val carDescription: String? = null,
    )

    @Transactional
    fun blockExpiredPhoneNumbers(months: Int): Int {
        //get all live phones
        val logEntries = logEntryService.getAllLastNMonths(months)
        logger().info("Log entries count = ${logEntries.size} by last $months months")
        val liveEntries = logEntries.map { it.phoneNumber }.toSet()
        logger().info("Uniq phones count = ${liveEntries.size} by last $months months")

        val findAllActive = accessService.findAllActive()
        logger().info("All phones count = ${findAllActive.size}")
        //Найти все телефоны в доступах, которыми не пользовались более 3-х месяцев
        val accessesForBlock = findAllActive.filterNot { liveEntries.contains(it.phoneNumber) }
        logger().info("Phones for block count = ${accessesForBlock.size}")

        //block access by id
        accessService.deactivateAccessByIds(accessesForBlock.mapNotNull { it.id }, LocalDateTime.now(), AccessBlockReasonEnum.EXPIRED)
        logger().info("Blocked phones count = ${accessesForBlock.size}")
        return accessesForBlock.size
    }

    fun addAvailablelAccessAreas() {
        val owners = ownerService.findAll()
        owners.forEach { owner ->
            ownerService.findRoomsByOwnerId(owner.id!!).let { rooms ->
                val areas = when {
                    rooms.any { it.type == RoomTypeEnum.FLAT } && rooms.any { it.type == RoomTypeEnum.GARAGE } -> listOf(1L, 2L)
                    rooms.any { it.type == RoomTypeEnum.FLAT } -> listOf(1L)
                    rooms.any { it.type == RoomTypeEnum.GARAGE } -> listOf(2L)
                    else -> emptyList()
                }
                if (areas.isNotEmpty()) {
                    if (owner.availableAccessArea == null) {
                        owner.availableAccessArea = mutableListOf()
                        owner.availableAccessArea?.addAll(areas)
                    } else {
                        owner.availableAccessArea?.clear()
                        owner.availableAccessArea?.addAll(areas)
                    }
                    ownerService.save(owner)
                }
            }
        }
    }
}