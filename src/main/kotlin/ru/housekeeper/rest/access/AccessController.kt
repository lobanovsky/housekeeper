package ru.housekeeper.rest.access

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.model.dto.access.CreateAccessRequest
import ru.housekeeper.model.dto.access.UpdateAccessRequest
import ru.housekeeper.service.AreaService
import ru.housekeeper.service.access.AccessService
import ru.housekeeper.utils.yyyyMMddHHmmssDateFormat
import java.nio.charset.Charset
import java.time.LocalDateTime

@CrossOrigin
@RestController
@RequestMapping("/access")
class AccessController(
    private val accessService: AccessService,
    private val areaService: AreaService,
) {

    //create the area access by phone number
    @PostMapping
    @Operation(summary = "Create the area access by the phone number")
    fun createAccess(
        @RequestBody createAccessRequest: CreateAccessRequest
    ) = accessService.create(createAccessRequest)

    //Edit the area access by phone number
    @PutMapping("/{access-id}")
    @Operation(summary = "Edit the area access by the phone number")
    fun updateAccess(
        @PathVariable("access-id") accessId: Long,
        @RequestBody updateAccessRequest: UpdateAccessRequest
    ) = accessService.update(accessId, updateAccessRequest)

    //Get accesses by room-id
    @GetMapping("/rooms/{room-id}")
    @Operation(summary = "Get the access by the room id")
    fun findByRoom(
        @PathVariable("room-id") roomId: Long,
        @RequestParam active: Boolean = true,
    ) = accessService.findByRoom(roomId, active)


    //Block the area access by phone number
    @DeleteMapping("/{access-id}")
    @Operation(summary = "Block the area access by the phone number")
    fun deleteAccess(
        @PathVariable("access-id") accessId: Long
    ) = accessService.deactivateAccess(accessId)


//    @GetMapping("/phones/{phone-number}")
//    @Operation(summary = "Get the access by the phone number")
//    fun findByPhone(
//        @PathVariable("phone-number") phoneNumber: String,
//        @RequestParam active: Boolean = true,
//    ) = accessService.findByPhoneNumber(phoneNumber, active)
//
//
//    @GetMapping("/cars/{car-number}")
//    @Operation(summary = "Get the access by the car number")
//    fun findByCarNumber(
//        @PathVariable("car-number") carNumber: String,
//        @RequestParam active: Boolean = true,
//    ) = accessService.findByCarNumber(carNumber, active)

    //export access to .csv by area
    @GetMapping("/export/{area-id}")
    @Operation(summary = "Export the access to .csv by the area id")
    fun exportAccess(
        @PathVariable("area-id") areaId: Long
    ): ResponseEntity<ByteArray> {
        val areas = areaService.findAll().associateBy { it.id }
        val area = areas[areaId] ?: return ResponseEntity.notFound().build()

        val fileName = "${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}_${area.id}.txt"
        val eldesContact = accessService.getEldesContact(areaId)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(eldesContact.joinToString(separator = "\r\n").toByteArray(Charset.forName("WINDOWS-1251"))
            )
    }

    //get info by car number
    @GetMapping("/overview/{plate-number}")
    @Operation(summary = "Get the overview by the plate number")
    fun overview(
        @PathVariable("plate-number") plateNumber: String,
        @RequestParam active: Boolean = true,
    ) = accessService.getOverview(plateNumber, active)

}