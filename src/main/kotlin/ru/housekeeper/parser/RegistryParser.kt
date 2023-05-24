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

class RegistryParser(private val file: MultipartFile) {

    fun parse(): List<RoomVO> {
        logger().info("Start parsing registry file ${file.originalFilename}")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheet("Собственники")
        logger().info("Parsing sheet: ${sheet.sheetName}")
        val rooms = sheetParser(sheet)
        logger().info("Parsed ${rooms.size} rooms")
        return rooms
    }

    private fun sheetParser(sheet: Sheet): List<RoomVO> {
        val numberNum = 0
        val squareNum = 1
        val addressNum = 5
        val cadastreNumberNum = 6
        val ownerNum = 7
        val descriptionNum = 8

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val rooms = mutableListOf<RoomVO>()
        val numberOfSkippingRows = 1
        for (i in numberOfSkippingRows..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            val number = when (row.getCell(numberNum).cellType) {
                CellType.NUMERIC -> row.getCell(numberNum).numericCellValue.toInt().toString()
                CellType.STRING -> row.getCell(numberNum).stringCellValue.trim()
                else -> ""
            }
            val square = row.getCell(squareNum).numericCellValue
            val street = row.getCell(addressNum).stringCellValue.trim()
            val cadastreNumber = row.getCell(cadastreNumberNum).stringCellValue.trim()
            val owners = row.getCell(ownerNum).stringCellValue.trim()
            val description = row.getCell(descriptionNum).stringCellValue.trim()

            rooms.add(
                RoomVO(
                    street = street,
                    ownerName = owners,
                    cadastreNumber = cadastreNumber,
                    number = number,
                    square = BigDecimal(square).setScale(2, RoundingMode.HALF_UP),
                    owners = owners.split(",").map { OwnerVO(fullName = it.trim()) }.toMutableSet(),
                    type = if (number.isNotEmpty()) RoomTypeEnum.FLAT else RoomTypeEnum.GARAGE,
                    certificate = description
                )
            )
        }
        return rooms
    }
}