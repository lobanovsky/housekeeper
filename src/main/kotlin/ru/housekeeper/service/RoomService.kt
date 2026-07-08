package ru.housekeeper.service

import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.OwnerContactPhoneResponse
import ru.housekeeper.model.dto.RoomOwnerContactsResponse
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.filter.RoomFilter
import ru.housekeeper.parser.RegistryParser
import ru.housekeeper.repository.access.AccessRepository
import ru.housekeeper.repository.owner.OwnerRepository
import ru.housekeeper.repository.room.RoomRepository
import ru.housekeeper.utils.MAX_SIZE_PER_PAGE
import ru.housekeeper.utils.entityNotfound
import ru.housekeeper.utils.logger
import java.math.BigDecimal

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val ownerRepository: OwnerRepository,
    private val accessRepository: AccessRepository,
//    private val ownerService: OwnerService,
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

//    @Transactional
//    fun parseAndSave(file: MultipartFile, checksum: String): HomeownerInfo {
//        val roomVOs = HomeownerAccountParser(file).parse()
//
//        logger().info("Parsed ${roomVOs.size} rooms")
//        var countSavedRoom = 0;
//        var countSavedOwner = 0;
//        roomVOs.forEach { roomVO ->
//            val room = roomVO.toRoom(checksum)
//
//            //save owners
//            val savedOwner = ownerService.saveIfNotExist(
//                if (roomVO.owners != null && roomVO.owners.size > 1) {
//                    OwnerVO(fullName = roomVO.owners.joinToString(separator = ",") { it.fullName }).toOwner(checksum)
//                } else if (roomVO.owners != null) {
//                    roomVO.owners.first().toOwner(checksum)
//                } else {
//                    OwnerVO(fullName = roomVO.ownerName).toOwner(checksum)
//                }
//            )
//            countSavedOwner++
//            room.owners.add(savedOwner.id)
//
//            //save room
//            val savedRoom = roomRepository.save(room)
//            savedRoom.id?.let { savedOwner.rooms.add(it) }
//            countSavedRoom++
//        }
//        logger().info("Saved rooms = [$countSavedRoom], owners = [$countSavedOwner]")
//        return HomeownerInfo(
//            countSavedRoom,
//            countSavedOwner,
//            roomVOs.sumOf { it.square },
//            roomVOs.sumOf { it.percentage })
//    }

    fun findAll(): List<Room> = roomRepository.findAll().toList()

    fun findById(roomId: Long): Room = roomRepository.findByIdOrNull(roomId) ?: entityNotfound("Помещение" to roomId)

    fun findWithFilter(pageNum: Int = 0, pageSize: Int = MAX_SIZE_PER_PAGE, filter: RoomFilter): Page<Room> =
        roomRepository.findAllWithFilter(pageNum, pageSize, filter)
            .also { logger().info("Found ${it.totalElements} rooms for filter: $filter}") }

    fun findByIds(ids: Set<Long>): List<Room> = roomRepository.findByIds(ids)

    fun findByRoomNumbersAndType(roomNumbers: Set<String>): List<Room> = roomRepository.findByRoomNumbersAndType(roomNumbers)

    fun findByNumberAndBuildingIdAndType(number: String, buildingId: Long, type: RoomTypeEnum): Room? = roomRepository.findByNumberAndBuildingIdAndType(number, buildingId, type)

    fun findByOwnerId(ownerId: Long): List<Room> = roomRepository.findByOwnerId(ownerId)

    fun findByBuildingIdsAndOwnerIds(buildingIds: Set<Long>, ownerId: Long): List<Room> = roomRepository.findByBuildingIdsAndOwnerIds(buildingIds, ownerId)

    fun findOwnerContacts(filter: RoomFilter, activeAccess: Boolean = true): List<RoomOwnerContactsResponse> {
        val rooms = findWithFilter(pageSize = MAX_SIZE_PER_PAGE, filter = filter).content
            .filter { filter.type != null || it.type == RoomTypeEnum.FLAT || it.type == RoomTypeEnum.GARAGE }

        val ownerIds = rooms.flatMap { it.owners }.toSet()
        if (ownerIds.isEmpty()) return emptyList()

        val ownersById = ownerRepository.findByIds(ownerIds)
            .associateBy { it.id }
        val phonesByOwnerId = accessRepository.findByOwnerIds(ownerIds, activeAccess)
            .groupBy { it.ownerId }
            .mapValues { (_, accesses) ->
                accesses
                    .groupBy { it.phoneNumber }
                    .map { (phoneNumber, phoneAccesses) ->
                        OwnerContactPhoneResponse(
                            phoneNumber = phoneNumber,
                            fullName = phoneAccesses
                                .firstNotNullOfOrNull { it.phoneLabel?.trim()?.takeIf(String::isNotBlank) },
                        )
                    }
                    .sortedBy { it.phoneNumber }
            }

        return rooms.flatMap { room ->
            room.owners.mapNotNull { ownerId ->
                val owner = ownersById[ownerId] ?: return@mapNotNull null
                RoomOwnerContactsResponse(
                    roomId = room.id,
                    buildingId = room.buildingId,
                    roomNumber = room.number,
                    roomType = room.type,
                    roomTypeDescription = room.type.description,
                    account = room.account,
                    square = room.square,
                    ownerId = owner.id!!,
                    ownerFullName = owner.fullName,
                    phones = phonesByOwnerId[ownerId] ?: emptyList(),
                )
            }
        }.sortedWith(
            compareBy<RoomOwnerContactsResponse> { it.buildingId }
                .thenBy { it.roomType }
                .thenBy { it.roomNumber }
                .thenBy { it.ownerFullName }
        )
    }
}
