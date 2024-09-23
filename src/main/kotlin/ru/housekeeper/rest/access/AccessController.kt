package ru.housekeeper.rest.access

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import ru.housekeeper.model.dto.access.AccessCreateRequest
import ru.housekeeper.model.dto.access.AccessUpdateRequest
import ru.housekeeper.service.access.AccessService

@CrossOrigin
@RestController
@RequestMapping("/access")
class AccessController(
    private val accessService: AccessService
) {

    //create the area access by phone number
    @PostMapping
    @Operation(summary = "Create the area access by the phone number (Were? -> Area, Who? -> Room)")
    fun createAccess(
        @RequestBody accessCreateRequest: AccessCreateRequest
    ) = accessService.createAccessToArea(accessCreateRequest)

    //Edit the area access by phone number
    @PutMapping("/{access-id}")
    @Operation(summary = "Edit the area access by the phone number")
    fun updateAccess(
        @PathVariable("access-id") accessId: Long,
        @RequestBody accessEditRequest: AccessUpdateRequest
    ) = accessService.updateAccessToArea(accessId, accessEditRequest)

    //remove the area access by phone number
    @DeleteMapping("/{access-id}")
    @Operation(summary = "Remove the area access by the phone number")
    fun deleteAccess(
        @PathVariable("access-id") accessId: Long
    ) = accessService.deleteAccess(accessId)


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


    @GetMapping("/cars/{car-number}")
    @Operation(summary = "Get the access by the car number")
    fun findByCarNumber(
        @PathVariable("car-number") carNumber: String,
        @RequestParam active: Boolean = true,
    ) = accessService.findByCarNumber(carNumber, active)

}