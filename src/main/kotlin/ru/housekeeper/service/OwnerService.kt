package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Owner
import ru.housekeeper.repository.owner.OwnerRepository
import ru.housekeeper.utils.entityNotfound

@Service
class OwnerService(
    private val ownerRepository: OwnerRepository
) {
    fun findByFullName(name: String): Owner? = ownerRepository.findByFullName(name)

    fun saveIfNotExist(owner: Owner): Owner = findByFullName(owner.fullName) ?: ownerRepository.save(owner)

    fun findById(id: Long): Owner = ownerRepository.findByIdOrNull(id) ?: entityNotfound("Собственник" to id)

    fun findAll(): List<Owner> = ownerRepository.findAll().toList()

    fun findByRoomId(roomId: Long, active: Boolean = true): List<Owner> = ownerRepository.findByRoomId(roomId, active)
}