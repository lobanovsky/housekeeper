package ru.housekeeper.service.access

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.enums.AccessBlockReasonEnum
import ru.housekeeper.exception.AccessToAreaException
import ru.housekeeper.model.dto.access.AccessResponse
import ru.housekeeper.model.dto.access.CreateAccessRequest
import ru.housekeeper.model.dto.access.OverviewResponse
import ru.housekeeper.model.dto.access.UpdateAccessRequest
import ru.housekeeper.model.dto.access.toAccessResponse
import ru.housekeeper.model.dto.access.toArea
import ru.housekeeper.model.dto.access.toCar
import ru.housekeeper.model.dto.access.toOverviewResponse
import ru.housekeeper.model.dto.eldes.EldesContact
import ru.housekeeper.model.entity.access.AccessEntity
import ru.housekeeper.repository.access.AccessRepository
import ru.housekeeper.service.AreaService
import ru.housekeeper.service.OwnerService
import ru.housekeeper.service.RoomService
import ru.housekeeper.utils.MAX_ELDES_LABEL_LENGTH
import ru.housekeeper.utils.PHONE_NUMBER_LENGTH
import ru.housekeeper.utils.beautifulPhonePrint
import java.time.LocalDateTime

@Service
class AccessService(
    private val accessRepository: AccessRepository,
    private val ownerService: OwnerService,
    private val areaService: AreaService,
    private val roomService: RoomService,
) {

    fun create(request: CreateAccessRequest, active: Boolean = true): List<AccessResponse> {
        if (request.accesses.isEmpty()) throw AccessToAreaException("Не указаны зоны доступа")
        phoneNumberValidator(request.accesses.first().phoneNumber)

        val result = mutableListOf<AccessResponse>()
        for (access in request.accesses) {
            val existAccess = findByOwnerIdAndPhone(request.ownerId, access.phoneNumber)
            //если собственник уже добавлял данный номер телефона
            if (existAccess != null && active) {
                throw AccessToAreaException("У собственника [${ownerService.findById(request.ownerId).fullName}], номер [${access.phoneNumber.beautifulPhonePrint()}] уже зарегистрирован")
            }
            val access = accessRepository.save(
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
            result.add(access.toAccessResponse(findAllArea()))
        }
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
        val existCars = existAccess.cars
        existCars?.clear()
        existCars?.addAll(request.cars?.map { it.toCar() } ?: emptyList())

        val updatedAccess = accessRepository.save(existAccess)
        return updatedAccess.toAccessResponse(findAllArea())
    }

    fun findAll() = accessRepository.findAll()

    fun findAllActive() = accessRepository.findAllActive()

    fun findAllArea() = areaService.findAll().associateBy({ it.id }, { it.name })

    fun findById(accessId: Long): AccessEntity = accessRepository.findByIdOrNull(accessId) ?: throw AccessToAreaException("Доступ не найден")

    fun findByOwnerIdAndPhone(ownerId: Long, phoneNumber: String, active: Boolean = true): AccessEntity? =
        accessRepository.findByPhoneNumberAndOwnerId(phoneNumber, ownerId, active)

    fun findByRoom(roomId: Long, active: Boolean): List<AccessResponse> {
        val owners = ownerService.findByRoomId(roomId, active)
        val existAccesses = owners.flatMap { accessRepository.findByOwnerId(it.id!!) }
        return existAccesses.map { it.toAccessResponse(findAllArea()) }
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
        return first.toOverviewResponse(
            allAreas = findAllArea(),
            ownerName = ownerService.findById(first.ownerId).fullName,
            ownerRooms = roomService.findByIds(ownerService.findById(first.ownerId).rooms).sortedBy { it.type }
                .joinToString { it.type.shortDescription + "" + it.number })

    }

    fun getEldesContact(areadId: Long): List<String> {
        val accesses = accessRepository.findByAreaId(areadId)
        val result = mutableListOf<String>()
        result.add("User Name;Tel Number;Relay No.;Sch.1 (1-true 0-false);Sch.2 (1-true 0-false);Sch.3 (1-true 0-false);Sch.4 (1-true 0-false);Sch.5 (1-true 0-false);Sch.6 (1-true 0-false);Sch.7 (1-true 0-false);Sch.8 (1-true 0-false);Year (Valid until);Month (Valid until);Day (Valid until);Hour (Valid until);Minute (Valid until);Ring Counter;Ring Counter Status")
        accesses.forEach { access ->
            val owner = ownerService.findById(access.ownerId)
            val firstRoom = roomService.findByIds(owner.rooms).sortedBy { it.type }.first();
            val label = firstRoom.number + "-" + firstRoom.type.name
            result.add(
                EldesContact(
                    userName = if (label.length > MAX_ELDES_LABEL_LENGTH) label.substring(0, MAX_ELDES_LABEL_LENGTH) else label,
                    telNumber = access.phoneNumber,
                ).toCSVLine()
            )
        }
        return result
    }

    private fun phoneNumberValidator(phoneNumber: String) {
        if (phoneNumber.first() != '7') throw AccessToAreaException("Номер телефона [$phoneNumber] должен начинаться с 7")
        if (phoneNumber.length != PHONE_NUMBER_LENGTH) throw AccessToAreaException("Номер телефона [$phoneNumber] должен содержать 11 цифр")
    }
}