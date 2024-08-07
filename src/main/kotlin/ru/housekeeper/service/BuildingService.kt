package ru.housekeeper.service

import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Building
import ru.housekeeper.repository.BuildingRepository

@Service
class BuildingService(
    private val buildingRepository: BuildingRepository
) {

    //get all buildings
    fun getAllBuildings(): List<Building> = buildingRepository.findAll().toList()
}