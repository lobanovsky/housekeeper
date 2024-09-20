package ru.housekeeper.service.access

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.exception.AccessToAreaException
import ru.housekeeper.model.dto.OwnerVO
import ru.housekeeper.model.dto.access.*
import ru.housekeeper.model.entity.Owner
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
    private val areaRepository: AreaRepository
) {

    data class AccessResponse(
        val phoneNumber: String,
        val success: Boolean
    )

    fun createAccessToArea(accessRequest: AccessRequest): List<AccessResponse> {
        val areas = accessRequest.areas
        val response = mutableListOf<AccessResponse>()
        for (phone in accessRequest.person.phones) {
            try {
                val phoneNumber = createAccessToArea(
                    ownerId = accessRequest.person.ownerId,
                    phone = phone,
                    areas = areas,
                    tenant = accessRequest.person.tenant
                )
                response.add(AccessResponse(phoneNumber.phoneNumber.beautifulPhonePrint(), true))
            } catch (e: AccessToAreaException) {
                logger().error("[$phone] Ошибка: $e")
                response.add(AccessResponse(phone.number, false))
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

    private fun getRoomWithOwnerByRoomId(roomId: Long): Pair<ru.housekeeper.model.entity.Room, Owner> {
        roomRepository.findByIdOrNull(roomId)?.let { room ->
            room.owners.firstOrNull()?.let { ownerId ->
                ownerRepository.findByIdOrNull(ownerId)?.let { owner ->
                    return Pair(room, owner)
                }
            }
        }
        entityNotfound("Помещение" to roomId)
    }

    fun findByRoom(roomId: Long, active: Boolean): AccessInfoVO {
        val owners = ownerRepository.findByRoomId(roomId, active)
        val rooms = roomRepository.findByIds(owners[0].rooms)
        val roomVOS = rooms.map { it.toRoomVO() }.sortedBy { it.type }
        val ownerId: Long = owners[0].id!!
        val accessInfos = accessInfoRepository.findByOwnerId(ownerId)
        val accessKeyVOS = accessKeyVOS(accessInfos)
        val accessInfoVO = AccessInfoVO(
            owner = OwnerVO(
                fullName = owners[0].fullName,
                ownerRooms = roomVOS
            ),
            keys = accessKeyVOS
        )
        return accessInfoVO
    }

    fun findByPhoneNumber(phoneNumber: String, active: Boolean): AccessInfoVO {
        val access = accessInfoRepository.findByPhoneNumber(phoneNumber, active) ?: entityNotfound("Номер телефона" to phoneNumber)
        val owner = ownerRepository.findByIdOrNull(access.ownerId) ?: entityNotfound("Владелец" to access.ownerId)
        val ownerVO = OwnerVO(
            fullName = owner.fullName,
            ownerRooms = roomRepository.findByIds(owner.rooms).map { it.toRoomVO() }
        )
        //get all areas
        val areas = areaRepository.findAllByIdIn(access.areas)
        areas.forEach { area ->
            logger().info("Получен доступ по номеру телефона: [${phoneNumber.beautifulPhonePrint()}] для зоны: ${area.name}")
        }
        return AccessInfoVO(
            owner = ownerVO,
            //keys sort by area type
            keys = accessKeyVOS(listOf(access))
        )
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
            )
        }.sortedBy { it.areas.first().type }
    }


}