package ru.housekeeper.service.access

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.enums.AccessBlockReasonEnum
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.exception.AccessToAreaException
import ru.housekeeper.model.dto.access.AccessResponse
import ru.housekeeper.model.dto.access.CarRequest
import ru.housekeeper.model.dto.access.CreateAccessRequest
import ru.housekeeper.model.dto.access.OverviewResponse
import ru.housekeeper.model.dto.access.UpdateAccessRequest
import ru.housekeeper.model.dto.access.toAccessResponse
import ru.housekeeper.model.dto.access.toArea
import ru.housekeeper.model.dto.access.toCar
import ru.housekeeper.model.dto.access.toOverviewResponse
import ru.housekeeper.model.dto.eldes.EldesContact
import ru.housekeeper.model.entity.access.AccessEntity
import ru.housekeeper.model.entity.access.Car
import ru.housekeeper.repository.access.AccessRepository
import ru.housekeeper.service.AreaService
import ru.housekeeper.service.OwnerService
import ru.housekeeper.service.RoomService
import ru.housekeeper.utils.MAX_ELDES_LABEL_LENGTH
import ru.housekeeper.utils.PHONE_NUMBER_LENGTH
import ru.housekeeper.utils.beautifulPhonePrint
import java.time.LocalDateTime
import kotlin.collections.mutableListOf

@Service
class AccessService(
    private val accessRepository: AccessRepository,
    private val ownerService: OwnerService,
    private val areaService: AreaService,
    private val roomService: RoomService,
) {

    fun create(request: CreateAccessRequest, active: Boolean = true): List<AccessResponse> {
        val access = request.accesses.first()
        if (access.areas.isEmpty()) throw AccessToAreaException("Не указаны зоны доступа")
        phoneNumberValidator(access.phoneNumber)

        val result = mutableListOf<AccessResponse>()
        val existAccess = findByOwnerIdAndPhone(request.ownerId, access.phoneNumber)
        //если собственник уже добавлял данный номер телефона
        if (existAccess != null && active) {
            throw AccessToAreaException("У собственника [${ownerService.findById(request.ownerId).fullName}], номер [${access.phoneNumber.beautifulPhonePrint()}] уже зарегистрирован")
        }
        val savedAccess = accessRepository.save(
            AccessEntity(
                ownerId = request.ownerId,
                areas = access.areas.map { it.toArea() }.toMutableList(),
                phoneNumber = access.phoneNumber,
                phoneLabel = access.phoneLabel,
                tenant = access.tenant,
                cars = access.cars?.map { it.toCar() }?.toMutableList(),
                active = active
            )
        )
        result.add(savedAccess.toAccessResponse(findAllArea()))
        return result
    }

    fun update(accessId: Long, request: UpdateAccessRequest): AccessResponse {
        val newAreas = request.areas
        if (newAreas.isEmpty()) throw AccessToAreaException("Не указаны зоны доступа")

        val existAccess = findById(accessId)
        //new label
        existAccess.phoneLabel = request.phoneLabel
        //tenant
        existAccess.tenant = request.tenant
        //new areas
        val existAreas = existAccess.areas
        existAreas.clear()
        existAreas.addAll(newAreas.map { it.toArea() })
        //new cars
        val carMerge = carMerge(request.cars ?: emptyList(), existAccess.cars ?: emptyList())
        if (carMerge.isNotEmpty()) existAccess.cars = mutableListOf()
        existAccess.cars?.clear()
        existAccess.cars?.addAll(carMerge)

        val updatedAccess = accessRepository.save(existAccess)
        return updatedAccess.toAccessResponse(findAllArea())
    }

    /**
     *  функция, на вход которой приходит список автомобилей, состоящий из двух свойств: номер авто и описание
     *  функция должна взять имеющийся список из базы данных и сделать слияние, то есть
     *  если нет данного номера авто в списке, то добавить
     *  если не пришёл номер, который есть в базе, то заблокировать - active=false
     *  если описание изменилось, то изменить, только описание
     *  если авто с таким же номером, а в базе заблокирован, то разблокировать
     */
    fun carMerge(cars: List<CarRequest>, existCars: List<Car>): List<Car> {
        val result = mutableListOf<Car>()
        val existCarsMap = existCars.associateBy { it.plateNumber }
        cars.forEach { car ->
            val existCar = existCarsMap[car.plateNumber]
            if (existCar == null) {
                result.add(Car(car.plateNumber, car.description))
            } else {
                if (existCar.active) {
                    if (existCar.description != car.description) {
                        existCar.description = car.description
                    }
                    result.add(existCar)
                } else {
                    existCar.active = true
                    existCar.description = car.description
                    result.add(existCar)
                }
            }
        }
        existCarsMap.forEach { (key, value) ->
            if (!cars.any { it.plateNumber == key }) {
                value.active = false
                result.add(value)
            }
        }
        return result
    }

    fun findAll() = accessRepository.findAll()

    fun findAllActive() = accessRepository.findAllActive()

    fun findAllArea() = areaService.findAll().associateBy({ it.id }, { it.name })

    fun findById(accessId: Long): AccessEntity = accessRepository.findByIdOrNull(accessId) ?: throw AccessToAreaException("Доступ не найден")

    fun findByOwnerIdAndPhone(ownerId: Long, phoneNumber: String, active: Boolean = true): AccessEntity? =
        accessRepository.findByPhoneNumberAndOwnerId(phoneNumber, ownerId, active)

    fun findByRoom(roomId: Long, active: Boolean): List<AccessResponse> {
        val owners = ownerService.findByRoomId(roomId, active)
        val existAccesses = owners.flatMap { accessRepository.findByOwnerId(it.id!!, active) }
        return existAccesses.map { it.toAccessResponse(findAllArea()) }.sortedBy { it.phoneNumber }
    }

    fun deactivateAccess(accessId: Long): AccessResponse {
        val access = findById(accessId)
        access.active = false
        val savedAccess = accessRepository.save(access)
        return savedAccess.toAccessResponse(findAllArea())
    }

    fun deactivateAccessByIds(accessIds: List<Long>, blockedDateTime: LocalDateTime, reason: AccessBlockReasonEnum) =
        accessRepository.deactivateByIds(accessIds, blockedDateTime, reason)


    fun getOverview(plateNumber: String, active: Boolean): OverviewResponse {
        val accesses = accessRepository.findByPlateNumber(plateNumber)
        if (accesses.isEmpty()) throw AccessToAreaException("Не найдено доступа по номеру автомобиля [$plateNumber]")
        val first = accesses.first()
        //find car by plate number
        val car =
            first.cars?.find { it.plateNumber.contains(plateNumber) } ?: throw AccessToAreaException("Не найдено автомобиля по номеру [$plateNumber]")
        return first.toOverviewResponse(
            allAreas = findAllArea(),
            ownerName = ownerService.findById(first.ownerId).fullName,
            ownerRooms = roomService.findByIds(ownerService.findById(first.ownerId).rooms).sortedBy { it.type }
                .joinToString { it.type.shortDescription + "" + it.number },
            car = car
        )
    }

    fun getEldesContact(areadId: Long): List<String> {
        val accesses = accessRepository.findByAreaId(areadId)
        val contacts = mutableListOf<EldesContact>()
        accesses.forEach { access ->
            val owner = ownerService.findById(access.ownerId)
            //если areaId = 1, то найти rooms с типом FLAT"
            //если areaId = 2, то взять из areas, places - первое значение
            var number: String = "-"
            var label: String = "-"
            when (areadId) {
                1L -> {
                    number = roomService.findByIds(owner.rooms).filter { it.type == RoomTypeEnum.FLAT }.first().number
                    val numbers = roomService.findByIds(owner.rooms).filter { it.type == RoomTypeEnum.FLAT }.joinToString(",") { it.number }
                    label = numbers + "-" + RoomTypeEnum.FLAT
                }

                2L -> {
                    number = access.areas.filter { it.areaId == 2L }.first().places?.first() ?: "-"
                    val numbers = access.areas.filter { it.areaId == 2L }.first().places?.joinToString(",") ?: "-"
                    label = numbers + "-" + RoomTypeEnum.GARAGE
                }
            }
            contacts.add(
                EldesContact(
                    sortField = number.padStart(3, '0'),
                    userName = if (label.length > MAX_ELDES_LABEL_LENGTH) label.substring(0, MAX_ELDES_LABEL_LENGTH) else label,
                    telNumber = access.phoneNumber,
                )
            )
        }
        val lines = contacts.sortedBy { it.sortField }.map { it.toCSVLine() }
        val result = mutableListOf<String>()
        result.add("User Name;Tel Number;Relay No.;Sch.1 (1-true 0-false);Sch.2 (1-true 0-false);Sch.3 (1-true 0-false);Sch.4 (1-true 0-false);Sch.5 (1-true 0-false);Sch.6 (1-true 0-false);Sch.7 (1-true 0-false);Sch.8 (1-true 0-false);Year (Valid until);Month (Valid until);Day (Valid until);Hour (Valid until);Minute (Valid until);Ring Counter;Ring Counter Status")
        result.addAll(lines)
        return result
    }

    private fun phoneNumberValidator(phoneNumber: String) {
        if (phoneNumber.first() != '7') throw AccessToAreaException("Номер телефона [$phoneNumber] должен начинаться с 7")
        if (phoneNumber.length != PHONE_NUMBER_LENGTH) throw AccessToAreaException("Номер телефона [$phoneNumber] должен содержать 11 цифр")
    }
}