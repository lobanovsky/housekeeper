package ru.housekeeper.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.entity.Debt
import ru.housekeeper.model.entity.Room
import ru.housekeeper.parser.DebtParser
import ru.housekeeper.repository.DebtRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class DebtService(
    private val debtRepository: DebtRepository,
    private val roomService: RoomService,
) {

    @Synchronized
    fun parseAndSave(file: MultipartFile): Int {
        val debts = DebtParser(file).parse()

        val existRooms = roomService.findAll()

        val debtEntities = mutableListOf<Debt>()
        val createDate = LocalDateTime.now()

        debts.filter { it.sum > BigDecimal.valueOf(5_000L) }.forEach {
            val fullNameAndSquare = getFullNameAndSquare(existRooms, it.roomNumber)
            debtEntities.add(
                Debt(
                    room = it.room,
                    tag = "2025-01",
                    account = it.account,
                    sum = it.sum,
                    fullName = fullNameAndSquare.first,
                    square = fullNameAndSquare.second,
                    roomNumber = it.roomNumber,
                    roomType = it.roomType,
                    debtType = it.debtType,
                    createDate = createDate,
                )
            )
        }
        debtRepository.saveAll(debtEntities)
        val size = debtEntities.size
        return size

    }

    private fun getFullNameAndSquare(rooms: List<Room>, roomNumber: String): Pair<String, BigDecimal> {
        val room = rooms.firstOrNull { it.number == roomNumber }
        return if (room != null) {
            room.ownerName to room.square
        } else {
            "" to BigDecimal.ZERO
        }
    }
}