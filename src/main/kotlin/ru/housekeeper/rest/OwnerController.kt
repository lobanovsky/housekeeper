package ru.housekeeper.rest

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.OwnerService

@CrossOrigin
@RestController
@RequestMapping("/owners")
class OwnerController(
    private val ownerService: OwnerService,
) {

    @GetMapping("/{ownerId}/rooms")
    fun getRoomsByOwnerId(@PathVariable ownerId: Long) = ownerService.findRoomsByOwnerId(ownerId)
}