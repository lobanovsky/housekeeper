package ru.housekeeper.service.access

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.housekeeper.enums.AccessBlockReasonEnum
import ru.housekeeper.exception.AccessToAreaException
import ru.housekeeper.model.dto.OwnerVO
import ru.housekeeper.model.dto.access.*
import ru.housekeeper.model.dto.eldes.EldesContact
import ru.housekeeper.model.entity.access.AccessInfo
import ru.housekeeper.model.response.InfoByPlateNumber
import ru.housekeeper.repository.AreaRepository
import ru.housekeeper.repository.access.AccessInfoRepository
import ru.housekeeper.repository.access.CarRepository
import ru.housekeeper.repository.owner.OwnerRepository
import ru.housekeeper.repository.room.RoomRepository
import ru.housekeeper.service.OwnerService
import ru.housekeeper.utils.*
import java.time.LocalDateTime

@Service
class AccessService(
    private val accessInfoRepository: AccessInfoRepository,
    private val ownerRepository: OwnerRepository,
    private val roomRepository: RoomRepository,
    private val areaRepository: AreaRepository,
    private val carService: CarService,
    private val careRepository: CarRepository,
    private val ownerService: OwnerService,
) {

    @Transactional
    fun deactivateAccess(
        accessId: Long,
        blockedDateTime: LocalDateTime = LocalDateTime.now(),
        blockReason: AccessBlockReasonEnum = AccessBlockReasonEnum.MANUAL
    ) {
        val accessInfo = accessInfoRepository.findByIdOrNull(accessId) ?: entityNotfound("Доступ" to accessId)
        accessInfo.id?.let { accessInfoRepository.deactivateById(it, blockedDateTime, blockReason) }
        accessInfo.id?.let { careRepository.deactivateById(it) }
    }

    @Transactional
    fun updateAccessToArea(accessId: Long, accessEditRequest: AccessUpdateRequest): AccessInfoVO {
        val areas = accessEditRequest.areas
        if (areas.isEmpty()) {
            throw AccessToAreaException("Не указаны зоны доступа")
        }

        val accessInfo = accessInfoRepository.findByIdOrNull(accessId)?.let { accessInfo ->
            accessInfo.phoneLabel = accessEditRequest.label?.trim()
            accessInfo.tenant = accessEditRequest.tenant
            accessInfo.areas.clear()
            accessInfo.areas.addAll(areas)
            accessInfoRepository.save(accessInfo)
        } ?: entityNotfound("Доступ" to accessId)

        carService.updateCars(accessId, accessEditRequest.cars ?: setOf())

        return findByOwner(accessInfo.ownerId)
    }

    @Transactional
    fun createAccessToArea(accessCreateRequest: AccessCreateRequest): List<AccessCreateResponse> {
        if (accessCreateRequest.areas.isEmpty()) {
            throw AccessToAreaException("Не указаны зоны доступа")
        }
        if (accessCreateRequest.person.phones.isEmpty()) {
            throw AccessToAreaException("Не указаны телефоны")
        }
        val response = mutableListOf<AccessCreateResponse>()
        for (phone in accessCreateRequest.person.phones) {
            try {
                val accessInfo = createAccessToArea(
                    ownerId = accessCreateRequest.person.ownerId,
                    accessPhone = phone,
                    areas = accessCreateRequest.areas,
                    tenant = phone.tenant
                )
                accessInfo.id?.let { phone.cars?.let { cars -> carService.createCarForAccesses(it, cars) } }
                response.add(AccessCreateResponse(accessInfo.id, accessInfo.phoneNumber.beautifulPhonePrint(), true))
            } catch (e: AccessToAreaException) {
                logger().error("[$phone] Ошибка: $e")
                response.add(AccessCreateResponse(phoneNumber = phone.number, success = false, reason = e.message))
            }
        }
        return response
    }

    //add new phone number for access
    private fun createAccessToArea(
        ownerId: Long,
        accessPhone: AccessPhone,
        areas: Set<Long>,
        tenant: Boolean
    ): AccessInfo {
        val phoneNumber = accessPhone.number.onlyNumbers()
        phoneNumberValidator(phoneNumber)
        val accessInfo = AccessInfo(
            ownerId = ownerId,
            phoneNumber = phoneNumber,
            phoneLabel = accessPhone.label?.trim(),
            tenant = tenant
        ).apply {
            this.areas.addAll(areas)
        }
        return accessInfoRepository.save(accessInfo)
    }

    fun findByRoom(roomId: Long, active: Boolean): AccessInfoVO {
        val owners = ownerRepository.findByRoomId(roomId, active)
        return findByOwner(owners[0].id!!)
    }

    fun findByPhoneNumber(phoneNumber: String, active: Boolean): AccessInfoVO {
        val access = accessInfoRepository.findByPhoneNumber(phoneNumber, active)
            ?: entityNotfound("Номер телефона" to phoneNumber)
        val owner = ownerRepository.findByIdOrNull(access.ownerId) ?: entityNotfound("Владелец" to access.ownerId)
        return findByOwner(owner.id!!)
    }

    fun findByCarNumber(carNumber: String, active: Boolean): AccessInfoVO? {
        val car = carService.findByCarNumber(carNumber, active) ?: entityNotfound("Автомобиль" to carNumber)
        val accessInfo =
            accessInfoRepository.findByIdOrNull(car.accessInfoId) ?: entityNotfound("Доступ" to car.accessInfoId)
        val owner =
            ownerRepository.findByIdOrNull(accessInfo.ownerId) ?: entityNotfound("Владелец" to accessInfo.ownerId)
        return findByOwner(owner.id!!)
    }

    private fun findByOwner(ownerId: Long): AccessInfoVO {
        val owner = ownerRepository.findByIdOrNull(ownerId) ?: entityNotfound("Владелец" to ownerId)
        val accessInfoVO = AccessInfoVO(
            owner = OwnerVO(
                id = owner.id,
                fullName = owner.fullName,
                ownerRooms = roomRepository.findByIds(owner.rooms).map { it.toRoomVO() }
            ),
            keys = accessKeyVOS(accessInfoRepository.findByOwnerId(ownerId))
        )
        return accessInfoVO
    }

    private fun accessKeyVOS(accessInfos: List<AccessInfo>): List<KeyVO> {
        return accessInfos.map { accessInfo ->
            KeyVO(
                id = accessInfo.id,
                phoneNumber = accessInfo.phoneNumber.beautifulPhonePrint(),
                phoneLabel = accessInfo.phoneLabel,
                tenant = accessInfo.tenant,
                areas = areaRepository.findAllByIdIn(accessInfo.areas).map { area ->
                    AreaVO(
                        id = area.id,
                        name = area.name,
                        type = area.type.name
                    )
                }.sortedBy { it.type },
                cars = carService.findByAccessInfo(accessInfo.id!!, true).map { car ->
                    CarVO(
                        id = car.id,
                        number = car.number,
                        description = car.description
                    )
                }
            )
        }.sortedBy { it.areas.first().type }
    }

    private fun phoneNumberValidator(phoneNumber: String, accessId: Long? = null) {
        if (phoneNumber.first() != '7') {
            throw AccessToAreaException("Номер телефона [$phoneNumber] должен начинаться с 7")
        }
        if (phoneNumber.length != PHONE_NUMBER_LENGTH) {
            throw AccessToAreaException("Номер телефона [$phoneNumber] должен содержать 11 цифр")
        }
        accessInfoRepository.findByPhoneNumber(phoneNumber)?.let {
            if (accessId == null || it.id != accessId) {
                val owner = ownerService.findById(it.ownerId)
                val rooms = owner?.let { it1 -> roomRepository.findByIds(it1.rooms) }?.map { it.toRoomVO() }
                throw AccessToAreaException("Данный номер [${phoneNumber}] уже зарегистрирован. Собственник: ${owner?.fullName}: ${rooms?.map { it.type.description + " " + it.number }}")
            }
        }
    }

    fun getEldesContact(areadId: Long): List<String> {
        val accessInfos = accessInfoRepository.findByAreaId(areadId)
        val contacts = mutableListOf<String>()
        contacts.add("User Name;Tel Number;Relay No.;Sch.1 (1-true 0-false);Sch.2 (1-true 0-false);Sch.3 (1-true 0-false);Sch.4 (1-true 0-false);Sch.5 (1-true 0-false);Sch.6 (1-true 0-false);Sch.7 (1-true 0-false);Sch.8 (1-true 0-false);Year (Valid until);Month (Valid until);Day (Valid until);Hour (Valid until);Minute (Valid until);Ring Counter;Ring Counter Status")
        accessInfos.forEach { accessInfo ->
            val owner =
                ownerRepository.findByIdOrNull(accessInfo.ownerId) ?: entityNotfound("Владелец" to accessInfo.ownerId)
            val firstRoom = roomRepository.findByIds(owner.rooms).sortedBy { it.type }.first();
            val label = firstRoom.number + "-" + firstRoom.type.name
            contacts.add(
                EldesContact(
                    userName = if (label.length > MAX_ELDES_LABEL_LENGTH) label.substring(
                        0,
                        MAX_ELDES_LABEL_LENGTH
                    ) else label,
                    telNumber = accessInfo.phoneNumber,
                ).toCSVLine()
            )
        }
        return contacts
    }

    fun getInfoByCarNumber(plateNumber: String, active: Boolean): InfoByPlateNumber {
        val car = carService.findByCarNumber(plateNumber, active) ?: entityNotfound("Автомобиль" to plateNumber)
        val accessInfo =
            accessInfoRepository.findByIdOrNull(car.accessInfoId) ?: entityNotfound("Доступ" to car.accessInfoId)
        val owner =
            ownerRepository.findByIdOrNull(accessInfo.ownerId) ?: entityNotfound("Владелец" to accessInfo.ownerId)
        return InfoByPlateNumber(
            ownerName = owner.fullName,
            ownerRooms = roomRepository.findByIds(owner.rooms).sortedBy { it.type }.joinToString { it.type.shortDescription + "" + it.number },
            phoneNumber = accessInfo.phoneNumber.beautifulPhonePrint(),
            phoneLabel = accessInfo.phoneLabel,
            tenant = accessInfo.tenant,
            carNumber = car.number,
            carDescription = car.description
        )
    }

}