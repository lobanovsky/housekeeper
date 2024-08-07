package ru.housekeeper.rest.access

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import ru.housekeeper.service.access.AccessService

@CrossOrigin
@RestController
@RequestMapping("/access")
class AccessController(
    private val accessService: AccessService
) {

    //create the area access by phone number
    @PostMapping("/areas")
    @Operation(summary = "Create the area access by the phone number (Were? -> Area, Who? -> Room)")
    fun createAccess(
        @RequestBody accessRequest: AccessRequest
    ): List<String> = accessService.createAccessToArea(accessRequest)

    data class AccessRequest(
        val phoneNumbers: Set<String>,
        val areas: Set<Long>,
        val rooms: Set<Room>,
        val tenant: Boolean
    )

    data class Room(
        val buildingId: Long,
        val roomIds: Set<Long>,
    )

    @GetMapping("/rooms/{room-id}")
    @Operation(summary = "Get the access by the room id")
    fun findByRoom(
        @PathVariable("room-id") roomId: Long,
        @RequestParam active: Boolean = true,
    ) = accessService.findByRoom(roomId, active)


    @GetMapping("/phones/{phone-number}")
    @Operation(summary = "Get the access by the phone number")
    fun findByPhone(
        @PathVariable("phone-number") phoneNumber: String,
        @RequestParam active: Boolean = true,
    ) = accessService.findByPhoneNumber(phoneNumber, active)


}