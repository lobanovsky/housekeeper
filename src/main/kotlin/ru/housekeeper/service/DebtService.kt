package ru.housekeeper.service

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.excel.toExcelDebt
import ru.housekeeper.model.entity.Debt
import ru.housekeeper.model.entity.Room
import ru.housekeeper.parser.DebtParser
import ru.housekeeper.repository.DebtRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
            val fullNameAndSquare = getFullNameAndSquare(existRooms, it.roomNumber, it.roomType)
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

    private fun getFullNameAndSquare(rooms: List<Room>, roomNumber: String, roomType: RoomTypeEnum): Pair<String, BigDecimal> {
        val room = rooms.firstOrNull { it.number == roomNumber && it.type == roomType }
        return if (room != null) {
            room.ownerName to room.square
        } else {
            "" to BigDecimal.ZERO
        }
    }


    fun replacePlaceholdersInDocx(inputFile: String, outputFile: String, replacements: Map<String, String>) {
        FileInputStream(File(inputFile)).use { fis ->
            val document = XWPFDocument(fis)

            for (paragraph in document.paragraphs) {
                for ((placeholder, value) in replacements) {
                    if (paragraph.text.contains("{$placeholder}")) {
                        paragraph.runs.forEach { run ->
                            run.setText(run.text().replace("{$placeholder}", value), 0)
                        }
                    }
                }
            }
            FileOutputStream(File(outputFile)).use { fos ->
                document.write(fos)
            }
        }
    }

    fun generateDebts() {
        val inputFile = "template.docx"
        val debts = debtRepository.findAll().filter { it.sum > BigDecimal.valueOf(5_000L) }

        val flats = debts.filter { it.roomType == RoomTypeEnum.FLAT }.sortedByDescending { it.sum }
        val garages = debts.filter { it.roomType == RoomTypeEnum.GARAGE }.sortedByDescending { it.sum }
        val offices = debts.filter { it.roomType == RoomTypeEnum.OFFICE }.sortedByDescending { it.sum }

        toExcelDebt(flats + garages + offices)

        debts.forEach {
            val replacements = mapOf(
                "fio" to it.fullName,
                "roomtype" to it.roomType.shortDescription,
                "roomnum" to it.roomNumber,
                "square" to it.square.toString(),
                "account" to it.account,
                "sum" to it.sum.toString(),
                "date" to "20 февраля 2025",
            )
            val outputFileName = "/Users/evgeny/Projects/tsn/housekeeper/etc/debt/${it.roomType.shortDescription}${it.roomNumber}_${it.fullName}.docx"
            replacePlaceholdersInDocx(inputFile, outputFileName, replacements)
        }
    }

}