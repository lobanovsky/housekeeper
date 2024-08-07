package ru.housekeeper.rest

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.BuildingService

@CrossOrigin
@RestController
@RequestMapping("/buildings")
class BuildingController(
    private val buildingService: BuildingService
) {

    @GetMapping
    fun findAll() = buildingService.getAllBuildings()

}