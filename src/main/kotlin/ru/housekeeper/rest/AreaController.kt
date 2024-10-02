package ru.housekeeper.rest

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.AreaService

@CrossOrigin
@RestController
@RequestMapping("/areas")
class AreaController(
    private val areaService: AreaService
) {

    @GetMapping
    fun findAll() = areaService.findAll()

}