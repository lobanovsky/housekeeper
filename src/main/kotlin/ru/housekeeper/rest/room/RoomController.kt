package ru.housekeeper.rest.room

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.RoomVO
import ru.housekeeper.model.filter.RoomFilter
import ru.housekeeper.service.RoomService
import ru.housekeeper.utils.toRoomVO

@CrossOrigin
@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomService: RoomService,
) {

    @GetMapping("/types")
    @Operation(summary = "Get all types of room")
    fun getRoomTypes(): List<RoomTypeResponse> = RoomTypeEnum.entries.map { RoomTypeResponse(it.name, it.description) }

    data class RoomTypeResponse(
        val name: String,
        val description: String,
    )

    @PostMapping
    @Operation(summary = "Get all rooms with filter")
    fun makeRoomsReport(
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @RequestBody filter: RoomFilter,
    ): Page<RoomVO> = roomService.findWithFilter(pageNum, pageSize, filter).toRoomVO(pageNum, pageSize)

}