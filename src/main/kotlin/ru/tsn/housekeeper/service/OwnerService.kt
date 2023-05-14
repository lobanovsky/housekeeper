package ru.tsn.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.tsn.housekeeper.model.entity.Owner
import ru.tsn.housekeeper.repository.OwnerRepository
import ru.tsn.housekeeper.utils.entityNotfound

@Service
class OwnerService(
    private val ownerRepository: OwnerRepository
) {

    fun findByFullName(name: String): Owner? = ownerRepository.findByFullName(name)

    fun saveIfNotExist(owner: Owner): Owner = findByFullName(owner.fullName) ?: ownerRepository.save(owner)

    fun findById(id: Long): Owner? = ownerRepository.findByIdOrNull(id) ?: entityNotfound("Owner" to id)

    fun findAll(): List<Owner> = ownerRepository.findAll().toList()
}