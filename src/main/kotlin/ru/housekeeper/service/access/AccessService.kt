package ru.housekeeper.service.access

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.exception.AccessToAreaException
import ru.housekeeper.model.dto.RoomVO
import ru.housekeeper.model.dto.access.AccessInfoVO
import ru.housekeeper.model.dto.access.AreaVO
import ru.housekeeper.model.entity.Owner
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.entity.access.AccessInfo
import ru.housekeeper.repository.AreaRepository
import ru.housekeeper.repository.OwnerRepository
import ru.housekeeper.repository.access.AccessPhoneRepository
import ru.housekeeper.repository.room.RoomRepository
import ru.housekeeper.rest.access.AccessController
import ru.housekeeper.utils.*

@Service
class AccessService(
    private val accessPhoneRepository: AccessPhoneRepository,
    private val ownerRepository: OwnerRepository,
    private val roomRepository: RoomRepository,
    private val areaRepository: AreaRepository
) {

    fun createAccessToArea(accessRequest: AccessController.AccessRequest): List<String> {
        val areas = accessRequest.areas
        val rooms = accessRequest.rooms
        val results = mutableListOf<String>()
        for (number in accessRequest.phoneNumbers) {
            try {
                val phoneNumber = createAccessToArea(number.onlyNumbers(), areas, rooms, accessRequest.tenant)
                results.add("[${phoneNumber.phoneNumber.beautifulPhonePrint()}] успешно добавлен")
            } catch (e: AccessToAreaException) {
                results.add("[$number] Ошибка: ${e.message}")
                logger().error("[$number] Ошибка: $e")
            }
        }
        return results
    }

    //add new phone number for access
    private fun createAccessToArea(
        phoneNumber: String,
        areas: Set<Long>,
        rooms: Set<AccessController.Room>,
        tenant: Boolean
    ): AccessInfo {
        if (phoneNumber.first() != '7') {
            throw AccessToAreaException("Номер телефона [$phoneNumber] должен начинаться с 7")
        }
        if (phoneNumber.length != PHONE_NUMBER_LENGTH) {
            throw AccessToAreaException("Номер телефона [$phoneNumber] должен содержать 11 цифр")
        }
        accessPhoneRepository.findByPhoneNumber(phoneNumber)?.let {
            throw AccessToAreaException("Данный номер [${phoneNumber.beautifulPhonePrint()}] уже зарегистрирован для доступа")
        }
        val accessInfo = AccessInfo(
            label = makeLabel(rooms),
            phoneNumber = phoneNumber,
            tenant = tenant
        ).apply {
            this.areas.addAll(areas)
            this.buildings.addAll(rooms.map { it.buildingId })
            this.rooms.addAll(rooms.flatMap { it.roomIds })
        }
        logger().info("Добавлен новый номер телефона: $accessInfo")
        return accessPhoneRepository.save(accessInfo)
    }

    private fun makeLabel(rooms: Set<AccessController.Room>): String {
        val roomNumbers = mutableListOf<String>()
        for (roomId in rooms.flatMap { it.roomIds }) {
            val (room, owner) = getRoomWithOwnerByRoomId(roomId)
            roomNumbers.add(room.number)
        }
        val label = roomNumbers.joinToString(", ")
        return if (label.length > MAX_ELDES_LABEL_LENGTH) label.substring(0, MAX_ELDES_LABEL_LENGTH) else label
    }

    private fun getRoomWithOwnerByRoomId(roomId: Long): Pair<Room, Owner> {
        roomRepository.findByIdOrNull(roomId)?.let { room ->
            room.owners.firstOrNull()?.let { ownerId ->
                ownerRepository.findByIdOrNull(ownerId)?.let { owner ->
                    return Pair(room, owner)
                }
            }
        }
        entityNotfound("Помещение" to roomId)
    }

    fun findByRoom(roomId: Long, active: Boolean): List<AccessInfoVO> {
        val accessInfos = accessPhoneRepository.findByRoomId(roomId, active)
        if (accessInfos.isEmpty()) return emptyList()
        val room = roomRepository.findByIdOrNull(roomId) ?: entityNotfound("Помещение" to roomId)
        logger().info("Получен доступ по идентификатору [$roomId] помещения [${room.type?.description}: ${room.number}]")
        return accessInfoVOS(accessInfos)
    }

    fun findByPhoneNumber(phoneNumber: String, active: Boolean): AccessInfoVO {
        val access = accessPhoneRepository.findByPhoneNumber(phoneNumber, active)
            ?: entityNotfound("Номер телефона" to phoneNumber)
        //get all areas
        val areas = areaRepository.findAllByIdIn(access.areas)
        areas.forEach { area ->
            logger().info("Получен доступ по номеру телефона: [${phoneNumber.beautifulPhonePrint()}] для зоны: ${area.name}")
        }
        return accessInfoVOS(listOf(access)).first()
    }

    private fun accessInfoVOS(accessInfos: List<AccessInfo>): List<AccessInfoVO> {
        return accessInfos.map { accessInfo ->
            AccessInfoVO(
                id = accessInfo.id,
                phoneNumber = accessInfo.phoneNumber,
                areas = areaRepository.findAllByIdIn(accessInfo.areas).map { area ->
                    AreaVO(
                        id = area.id,
                        name = area.name,
                        type = area.type.name
                    )
                },
                rooms = accessInfo.rooms.map {
                    val (r, o) = getRoomWithOwnerByRoomId(it)
                    RoomVO(
                        id = r.id,
                        building = r.building,
                        number = r.number,
                        ownerName = o.fullName,
                        square = r.square,
                        type = r.type,
                    )
                }
            )
        }
    }


}