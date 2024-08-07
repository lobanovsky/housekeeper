package ru.housekeeper.service

import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Area
import ru.housekeeper.repository.AreaRepository

@Service
class AreaService(
    private val areaRepository: AreaRepository
) {

    //get all areas
    fun getAllAreas(): List<Area> = areaRepository.findAll().toList()
}