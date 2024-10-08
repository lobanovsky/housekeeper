package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.model.dto.RoomVO
import ru.housekeeper.model.entity.OwnerEntity
import ru.housekeeper.repository.owner.OwnerRepository
import ru.housekeeper.utils.entityNotfound
import ru.housekeeper.utils.toRoomVO

@Service
class OwnerService(
    private val ownerRepository: OwnerRepository,
    private val roomService: RoomService,
) {
    fun findByFullName(name: String): OwnerEntity? = ownerRepository.findByFullName(name)

    fun saveIfNotExist(ownerEntity: OwnerEntity): OwnerEntity = findByFullName(ownerEntity.fullName) ?: ownerRepository.save(ownerEntity)

    fun findById(id: Long): OwnerEntity = ownerRepository.findByIdOrNull(id) ?: entityNotfound("Собственник" to id)

    fun findAll(): List<OwnerEntity> = ownerRepository.findAll().toList()

    fun findByRoomId(roomId: Long, active: Boolean = true): List<OwnerEntity> = ownerRepository.findByRoomId(roomId, active)

    fun findRoomsByOwnerId(ownerId: Long): List<RoomVO> = roomService.findByOwnerId(ownerId).map { it.toRoomVO() }

    fun save(ownerEntity: OwnerEntity): OwnerEntity = ownerRepository.save(ownerEntity)

}