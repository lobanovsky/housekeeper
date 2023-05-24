package ru.housekeeper.service

import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.OwnerVO
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.filter.RoomFilter
import ru.housekeeper.parser.HomeownerAccountParser
import ru.housekeeper.parser.RegistryParser
import ru.housekeeper.repository.room.RoomRepository
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
                if (roomVO.owners.size > 1) {
                    OwnerVO(fullName = roomVO.owners.joinToString(separator = ",") { it.fullName }).toOwner(checksum)
                } else {
                    roomVO.owners.first().toOwner(checksum)
                }
            )
            countSavedOwner++
            room.owners.add(savedOwner.id)

            //save room
            val savedRoom = roomRepository.save(room)
            savedOwner.rooms.add(savedRoom.id)
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

    fun findWithFilter(pageNum: Int, pageSize: Int, filter: RoomFilter): Page<Room> =
        roomRepository.findAllWithFilter(pageNum, pageSize, filter)

}