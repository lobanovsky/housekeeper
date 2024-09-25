package ru.housekeeper.rest.room

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.RoomVO
import ru.housekeeper.model.filter.RoomFilter
import ru.housekeeper.service.BuildingService
import ru.housekeeper.service.RoomService
import ru.housekeeper.utils.MAX_SIZE_PER_PAGE
import ru.housekeeper.utils.toRoomVO

@CrossOrigin
@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomService: RoomService,
    private val buildingService: BuildingService,
) {

    //Get Room By Id
    @GetMapping("/{id}")
    @Operation(summary = "Get room by id")
    fun getRoomById(@PathVariable id: Long): RoomVO = roomService.findById(id).toRoomVO()

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

    /**
     * Вернуть двумерный массив: дом с квартирами с разбивкой по этажам
     * строки - этажи с квартирами
     * столбцы - квартиры
     *
     * @param buildingId - идентификатор дома
     */
    @GetMapping("/building-structure/{buildingId}")
    @Operation(summary = "Get all rooms by building id")
    fun getBuildingStructure(
        @PathVariable buildingId: Long
    ): List<FloorResponse> {
        val pageNum = 0
        val pageSize = MAX_SIZE_PER_PAGE
        val rooms = roomService.findWithFilter(pageNum, pageSize, filter = RoomFilter(building = buildingId))
            .toRoomVO(pageNum, pageSize).content
        val offices = rooms.filter { it.type == RoomTypeEnum.OFFICE }
        val flats = rooms.filter { it.type == RoomTypeEnum.FLAT }
        val garages = rooms.filter { it.type == RoomTypeEnum.GARAGE }

        val numberOfApartmentsPerFloor = buildingService.getNumberOfApartmentsPerFloor(buildingId)

        /**
         * Необходимо создать структуру дома
         * офисы всегда на первом этаже
         * помещения на остальных этажах
         * количество помещений на этаже задаётся через парамер numberOfApartmentsPerFloor
         * Если есть и квартиры и гаражи, то гаражи должны быть под домом с нумерацией -1..-N
         * Если есть только гаражи, то нумерация от 1 до N
         */
        val floors = mutableListOf<FloorResponse>()
        if (offices.isNotEmpty()) {
            floors.add(FloorResponse(1, offices))
        }
        flats.chunked(numberOfApartmentsPerFloor).forEachIndexed { index, chunk ->
            floors.add(FloorResponse(index + 2, chunk))
        }
        garages.chunked(numberOfApartmentsPerFloor).forEachIndexed { index, chunk ->
            floors.add(FloorResponse(-index - 1, chunk))
        }
        return floors
    }

    data class FloorResponse(
        val floor: Int,
        val rooms: List<RoomVO>
    )

}