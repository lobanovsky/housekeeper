package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.excel.*
import ru.housekeeper.service.RoomService
import ru.housekeeper.utils.*
import java.time.LocalDateTime

@CrossOrigin
@RestController
@RequestMapping("/reports/rooms")
class RoomReportController(
    private val roomService: RoomService,
) {

    @GetMapping
    @Operation(summary = "Print rooms")
    fun makeRoomsReport(): ResponseEntity<ByteArray> {
        val rooms = roomService.findAll()
        val fileName = "Rooms_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(toExcelRooms(rooms = rooms))
    }

}