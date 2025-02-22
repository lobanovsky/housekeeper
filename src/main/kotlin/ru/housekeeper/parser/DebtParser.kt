package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.DebtTypeEnum
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.DebtVO
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.math.RoundingMode

class DebtParser(private val file: MultipartFile) {

    fun parse(): List<DebtVO> {
        logger().info("Start parsing debt file ${file.originalFilename}")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheetFlat = workbook.getSheet("Квартиры")
        val sheetGarage = workbook.getSheet("Машиноместа")
        logger().info("Parsing sheet: ${sheetFlat.sheetName}")
        val rooms = sheetParser(sheetFlat)
        val garage = sheetParser(sheetGarage)
        return rooms + garage
    }

    private fun sheetParser(sheet: Sheet): List<DebtVO> {
        val roomNum = 0
        val accountNum = 1
        val sumNum = 2

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")
        val debts = mutableListOf<DebtVO>()

        for (i in 0..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            val room = row.getCell(roomNum).stringCellValue.trim()
            val account = row.getCell(accountNum).stringCellValue.trim()
            val sum = row.getCell(sumNum).numericCellValue

            logger().info("Room: $room, account: $account, sum: $sum")

            val roomInfo = parseRoomInfo(room)
            debts.add(
                DebtVO(
                    room = room,
                    account = account,
                    sum = BigDecimal(sum).setScale(2, RoundingMode.HALF_UP),
                    roomNumber = roomInfo.first,
                    roomType = roomInfo.second,
                    debtType = DebtTypeEnum.MAJOR_REPAIRS
                )
            )
        }
        return debts
    }

    private fun parseRoomInfo(input: String): Pair<String, RoomTypeEnum> {
        val patterns = listOf(
            "кв\\.\\s*(\\d+)" to RoomTypeEnum.FLAT,
            "кв\\.\\s*Оф\\.\\s*(\\d+)" to RoomTypeEnum.OFFICE,
            "кв\\.\\s*А/м\\s*(\\d+)" to RoomTypeEnum.GARAGE
        )

        for ((pattern, type) in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(input)
            if (match != null) {
                val number = match.groupValues[1]
                if (true) {
                    return number to type
                }
            }
        }
        return "" to RoomTypeEnum.FLAT
    }

}