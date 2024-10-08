package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.model.dto.RoomVO
import ru.housekeeper.model.entity.Owner
import ru.housekeeper.repository.owner.OwnerRepository
import ru.housekeeper.utils.entityNotfound
import ru.housekeeper.utils.toRoomVO

@Service
class OwnerService(
    private val ownerRepository: OwnerRepository,
    private val roomService: RoomService,
) {
    fun findByFullName(name: String): Owner? = ownerRepository.findByFullName(name)

    fun saveIfNotExist(owner: Owner): Owner = findByFullName(owner.fullName) ?: ownerRepository.save(owner)

    fun findById(id: Long): Owner = ownerRepository.findByIdOrNull(id) ?: entityNotfound("Собственник" to id)

    fun findAll(): List<Owner> = ownerRepository.findAll().toList()

    fun findByRoomId(roomId: Long, active: Boolean = true): List<Owner> = ownerRepository.findByRoomId(roomId, active)

    fun findRoomsByOwnerId(ownerId: Long): List<RoomVO> = roomService.findByOwnerId(ownerId).map { it.toRoomVO() }

}