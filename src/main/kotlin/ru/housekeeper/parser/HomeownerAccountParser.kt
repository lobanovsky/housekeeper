package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.OwnerVO
import ru.housekeeper.model.dto.RoomVO
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.math.RoundingMode

class HomeownerAccountParser(private val file: MultipartFile) {

    fun parse(): List<RoomVO> {
        logger().info("Start parsing account file ${file.originalFilename}")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        logger().info("Parsing sheet: ${sheet.sheetName}")
        val rooms = sheetParser(sheet)
        logger().info("Parsed ${rooms.size} rooms")
        return rooms
    }

    private fun sheetParser(sheet: Sheet): List<RoomVO> {
        val streetNum = 2
        val buildingNum = 3
        val numberNum = 4
        val fullNameNum = 6
        val accountNum = 7
        val squareNum = 9

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")
        val rooms = mutableListOf<RoomVO>()
        val numberOfSkippingRows = 1
        for (i in numberOfSkippingRows..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            val street = row.getCell(streetNum).stringCellValue.trim()
            val building = when (row.getCell(buildingNum).cellType) {
                CellType.NUMERIC -> row.getCell(buildingNum).numericCellValue.toInt().toString()
                CellType.STRING -> row.getCell(buildingNum).stringCellValue.trim()
                else -> ""
            }
            val number = when (row.getCell(numberNum).cellType) {
                CellType.NUMERIC -> row.getCell(numberNum).numericCellValue.toInt().toString()
                CellType.STRING -> row.getCell(numberNum).stringCellValue.trim()
                else -> ""
            }
            val fullName = row.getCell(fullNameNum).stringCellValue.trim()
            val account = row.getCell(accountNum).stringCellValue.trim()
            val square = row.getCell(squareNum).numericCellValue
            logger().info("Parsed row: $street, $building, $number, $fullName, $account, $square")

            val accountPrefix = account.trimStart('0')[0]
            if (accountPrefix.digitToInt() > 3) {
                logger().info("Account prefix is $accountPrefix, skipping")
                continue
            }
            val owners = (fullName.substringBefore("(").trim()).trim()
            rooms.add(
                RoomVO(
                    street = street,
                    building = building,
                    account = account,
                    ownerName = owners,
                    number = account.trimStart('0').drop(1).trimStart('0'),
                    square = BigDecimal(square).setScale(2, RoundingMode.HALF_UP),
                    owners = owners.split(",").map { OwnerVO(fullName = it.trim()) }.toMutableSet(),
                    type = if (number.startsWith("Оф")) RoomTypeEnum.OFFICE else if (number.startsWith("А/м")) RoomTypeEnum.GARAGE else RoomTypeEnum.FLAT
                )
            )
        }
        return rooms
    }
}