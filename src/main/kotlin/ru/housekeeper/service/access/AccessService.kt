package ru.housekeeper.service.access

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.exception.AccessToAreaException
import ru.housekeeper.model.dto.OwnerVO
import ru.housekeeper.model.dto.access.*
import ru.housekeeper.model.entity.access.AccessInfo
import ru.housekeeper.repository.AreaRepository
import ru.housekeeper.repository.access.AccessInfoRepository
import ru.housekeeper.repository.owner.OwnerRepository
import ru.housekeeper.repository.room.RoomRepository
import ru.housekeeper.utils.*

@Service
class AccessService(
    private val accessInfoRepository: AccessInfoRepository,
    private val ownerRepository: OwnerRepository,
    private val roomRepository: RoomRepository,
    private val areaRepository: AreaRepository,
    private val carService: CarService,
) {

    data class AccessResponse(
        val id: Long? = null,
        val phoneNumber: String,
        val success: Boolean
    )

    fun createAccessToArea(accessRequest: AccessRequest): List<AccessResponse> {
        val areas = accessRequest.areas
        val response = mutableListOf<AccessResponse>()
        for (phone in accessRequest.person.phones) {
            try {
                val accessInfo = createAccessToArea(
                    ownerId = accessRequest.person.ownerId,
                    phone = phone,
                    areas = areas,
                    tenant = accessRequest.person.tenant
                )
                response.add(AccessResponse(accessInfo.id, accessInfo.phoneNumber.beautifulPhonePrint(), true))
            } catch (e: AccessToAreaException) {
                logger().error("[$phone] Ошибка: $e")
                response.add(AccessResponse(phoneNumber = phone.number, success = false))
            }
        }
        return response
    }

    //add new phone number for access
    private fun createAccessToArea(
        ownerId: Long,
        phone: Phone,
        areas: Set<Long>,
        tenant: Boolean
    ): AccessInfo {
        val phoneNumber = phone.number.onlyNumbers()
        val phoneLabel = phone.label?.trim()
        if (phoneNumber.first() != '7') {
            throw AccessToAreaException("Номер телефона [$phoneNumber] должен начинаться с 7")
        }
        if (phoneNumber.length != PHONE_NUMBER_LENGTH) {
            throw AccessToAreaException("Номер телефона [$phoneNumber] должен содержать 11 цифр")
        }
        accessInfoRepository.findByPhoneNumber(phoneNumber)?.let {
            throw AccessToAreaException("Данный номер [${phoneNumber}] уже зарегистрирован для доступа")
        }
        val accessInfo = AccessInfo(
            ownerId = ownerId,
            phoneNumber = phoneNumber,
            phoneLabel = phoneLabel,
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

    private fun findByOwner(ownerId: Long): AccessInfoVO {
        val owner = ownerRepository.findByIdOrNull(ownerId) ?: entityNotfound("Владелец" to ownerId)
        val accessInfoVO = AccessInfoVO(
            owner = OwnerVO(
                fullName = owner.fullName,
                ownerRooms = roomRepository.findByIds(owner.rooms).map { it.toRoomVO() }
            ),
            keys = accessKeyVOS(accessInfoRepository.findByOwnerId(ownerId))
        )
        return accessInfoVO
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


}