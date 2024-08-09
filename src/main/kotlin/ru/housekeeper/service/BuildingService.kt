package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Building
import ru.housekeeper.repository.BuildingRepository
import ru.housekeeper.utils.entityNotfound

@Service
class BuildingService(
    private val buildingRepository: BuildingRepository
) {

    //get all buildings
    fun getAllBuildings(): List<Building> = buildingRepository.findAll().toList()

    fun getNumberOfApartmentsPerFloor(buildingId: Long): Int =
        (buildingRepository.findByIdOrNull(buildingId) ?: entityNotfound("Дом" to buildingId)).let { building ->
            return building.numberOfApartmentsPerFloor
        }
}