package ru.housekeeper.rest.access

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.excel.toExcelAccesses
import ru.housekeeper.model.dto.access.AccessResponse
import ru.housekeeper.model.dto.access.CreateAccessRequest
import ru.housekeeper.model.dto.access.UpdateAccessRequest
import ru.housekeeper.service.AreaService
import ru.housekeeper.service.OwnerService
import ru.housekeeper.service.RoomService
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
    private val roomService: RoomService,
    private val ownerService: OwnerService,
) {

    @PostMapping
    @Operation(summary = "Создание доступа к территории по номеру телефона")
    fun createAccess(
        @RequestBody createAccessRequest: CreateAccessRequest
    ) = accessService.create(createAccessRequest)

    @PutMapping("/{access-id}")
    @Operation(summary = "Редактирование доступа к территории по номеру телефона")
    fun updateAccess(
        @PathVariable("access-id") accessId: Long,
        @RequestBody updateAccessRequest: UpdateAccessRequest
    ) = accessService.update(accessId, updateAccessRequest)

    @GetMapping("/rooms/{room-id}")
    @Operation(summary = "Получить доступы по id комнаты")
    fun findByRoom(
        @PathVariable("room-id") roomId: Long,
        @RequestParam active: Boolean = true,
    ): List<AccessResponse> = accessService.findByRoom(roomId, active)

    @GetMapping("/owners/{owner-id}")
    @Operation(summary = "Получить доступы по id владельца")
    fun findByOwner(
        @PathVariable("owner-id") ownerId: Long,
        @RequestParam active: Boolean = true,
    ): List<AccessResponse> = accessService.findByOwner(ownerId, active)


    @DeleteMapping("/{access-id}")
    @Operation(summary = "Заблокировать доступ к территории по номеру телефона")
    fun deleteAccess(
        @PathVariable("access-id") accessId: Long
    ) = accessService.deactivateAccess(accessId)


    @GetMapping("/phones/{phone-number}")
    @Operation(summary = "Get the access by the phone number")
    fun findByPhone(
        @PathVariable("phone-number") phoneNumber: String,
        @RequestParam active: Boolean = true,
    ) = accessService.findByPhoneNumber(phoneNumber, active)


//    @GetMapping("/cars/{car-number}")
//    @Operation(summary = "Get the access by the car number")
//    fun findByCarNumber(
//        @PathVariable("car-number") carNumber: String,
//        @RequestParam active: Boolean = true,
//    ) = accessService.findByCarNumber(carNumber, active)

    //export access to .csv by area
    @GetMapping("/export/eldes/{area-id}")
    @Operation(summary = "Экспорт доступов в Eldes-формат по id территории")
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

    @GetMapping("/overview/{plate-number}")
    @Operation(summary = "Получить обзор доступа по номеру автомобиля")
    fun overview(
        @PathVariable("plate-number") plateNumber: String,
        @RequestParam active: Boolean = true,
    ) = accessService.getOverview(plateNumber.trim().lowercase(), active)

    //export accesses to excel
    @GetMapping("/export")
    @Operation(summary = "Export the accesses to excel")
    fun exportAccesses(): ResponseEntity<ByteArray> {
        val accesses = accessService.findAllActive()
        val areas = areaService.findAll()
        val fileName = "Accesses_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(toExcelAccesses(areas, accesses, roomService))
    }
}