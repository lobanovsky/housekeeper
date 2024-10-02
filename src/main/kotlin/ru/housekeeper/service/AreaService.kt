package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Area
import ru.housekeeper.repository.AreaRepository
import ru.housekeeper.utils.entityNotfound

@Service
class AreaService(
    private val areaRepository: AreaRepository
) {

    fun findAll(): List<Area> = areaRepository.findAll().toList()

    fun findById(id: Long): Area = areaRepository.findByIdOrNull(id) ?: entityNotfound("Зона доступа" to id)

    fun findAllByIdIn(ids: Set<Long>): List<Area> = areaRepository.findAllByIdIn(ids)

}