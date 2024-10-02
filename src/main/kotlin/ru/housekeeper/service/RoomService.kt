package ru.housekeeper.service

import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.OwnerVO
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.filter.RoomFilter
import ru.housekeeper.parser.HomeownerAccountParser
import ru.housekeeper.parser.RegistryParser
import ru.housekeeper.repository.room.RoomRepository
import ru.housekeeper.utils.MAX_SIZE_PER_PAGE
import ru.housekeeper.utils.entityNotfound
import ru.housekeeper.utils.logger
import java.math.BigDecimal

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val ownerService: OwnerService,
) {

    data class RegistryInfo(
        val totalSize: Int,
        val flatsWithCertSize: Int,
        val garageOrOfficesWithCertSize: Int,
    )

    fun parseAndSaveRegistry(file: MultipartFile, checksum: String): RegistryInfo {
        val roomVOs = RegistryParser(file).parse()
        logger().info("Parsed ${roomVOs.size} rooms")
        var countFlats = 0;
        var countGarageOrOffices = 0;
        roomVOs.forEach { roomVO ->
            if (roomVO.number.isNotEmpty()) {
                roomRepository.findByNumberAndType(roomVO.number)?.let {
                    logger().info("Room with number ${roomVO.number} already exist")
                    it.certificate = roomVO.certificate
                    it.cadastreNumber = roomVO.cadastreNumber
                }
                countFlats++
            }
            if (roomVO.number.isEmpty()) {
                val existRooms =
                    roomRepository.findGarageOrOfficeByOwnerNameAndSquare(roomVO.ownerName.lowercase(), roomVO.square)
                if (existRooms.isEmpty()) {
                    logger().info("Room with ownerName ${roomVO.ownerName} and square ${roomVO.square} not found")
                }
                if (existRooms.isNotEmpty()) {
                    logger().info("Room with ownerName ${roomVO.ownerName} and square ${roomVO.square} already exist")
                    val room = existRooms.firstOrNull { it.certificate == null }
                    room?.certificate = roomVO.certificate
                    room?.cadastreNumber = roomVO.cadastreNumber
                }
                countGarageOrOffices++
            }
        }
        return RegistryInfo(roomVOs.size, countFlats, countGarageOrOffices)
    }

    data class HomeownerInfo(
        val roomSize: Int,
        val ownerSize: Int,
        val totalSquare: BigDecimal,
        val totalPercentage: BigDecimal,
    )

    @Transactional
    fun parseAndSave(file: MultipartFile, checksum: String): HomeownerInfo {
        val roomVOs = HomeownerAccountParser(file).parse()

        logger().info("Parsed ${roomVOs.size} rooms")
        var countSavedRoom = 0;
        var countSavedOwner = 0;
        roomVOs.forEach { roomVO ->
            val room = roomVO.toRoom(checksum)

            //save owners
            val savedOwner = ownerService.saveIfNotExist(
                if (roomVO.owners != null && roomVO.owners.size > 1) {
                    OwnerVO(fullName = roomVO.owners.joinToString(separator = ",") { it.fullName }).toOwner(checksum)
                } else if (roomVO.owners != null) {
                    roomVO.owners.first().toOwner(checksum)
                } else {
                    OwnerVO(fullName = roomVO.ownerName).toOwner(checksum)
                }
            )
            countSavedOwner++
            room.owners.add(savedOwner.id)

            //save room
            val savedRoom = roomRepository.save(room)
            savedRoom.id?.let { savedOwner.rooms.add(it) }
            countSavedRoom++
        }
        logger().info("Saved rooms = [$countSavedRoom], owners = [$countSavedOwner]")
        return HomeownerInfo(
            countSavedRoom,
            countSavedOwner,
            roomVOs.sumOf { it.square },
            roomVOs.sumOf { it.percentage })
    }

    fun findAll(): List<Room> = roomRepository.findAll().toList()

    fun findByRoomNumbersAndType(roomNumbers: Set<String>): List<Room> =
        roomRepository.findByRoomNumbersAndType(roomNumbers)

    fun findByRoomNumberAndType(number: String, type: RoomTypeEnum): Room? =
        roomRepository.findByNumberAndType(number, type)

    fun findByNumberAndBuildingId(number: String, buildingId: Long): List<Room>? =
        roomRepository.findByNumberAndBuildingId(number, buildingId)

    fun findWithFilter(pageNum: Int = 0, pageSize: Int = MAX_SIZE_PER_PAGE, filter: RoomFilter): Page<Room> =
        roomRepository.findAllWithFilter(pageNum, pageSize, filter)
            .also { logger().info("Found ${it.totalElements} rooms for filter: $filter}") }

    fun findById(roomId: Long): Room = roomRepository.findByIdOrNull(roomId) ?: entityNotfound("Помещение" to roomId)

    fun findByIds(ids: Set<Long>): List<Room> = roomRepository.findByIds(ids)

}