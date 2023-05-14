package ru.tsn.housekeeper.parser.counter

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.tsn.housekeeper.enums.counter.CounterTypeEnum
import ru.tsn.housekeeper.model.dto.counter.WaterCounterVO
import ru.tsn.housekeeper.utils.logger

class WaterCounterParser(private val file: MultipartFile) {

    fun parse(): List<WaterCounterVO> {
        logger().info("Start parsing water counter file ${file.originalFilename}")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)
        logger().info("Parsing sheet: ${sheet.sheetName}")
        val counters = sheetParser(sheet)
        logger().info("Parsed ${counters.size} counters")
        return counters
    }

    private fun sheetParser(sheet: Sheet): List<WaterCounterVO> {
        val roomNum = 1
        val hotNum = 2
        val coldNum = 4

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val counters = mutableListOf<WaterCounterVO>()
        val numberOfSkippingRows = 9
        for (i in numberOfSkippingRows..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val roomNumber = row.getCell(roomNum).stringCellValue.trim()
            roomNumber.toIntOrNull() ?: continue
            val hotNumber = getStringValue(row.getCell(hotNum))
            val coldNumber = getStringValue(row.getCell(coldNum))
            logger().info("Parsed row $i: room: $roomNumber hot: $hotNumber cold: $coldNumber")
            val hotCounter = WaterCounterVO(
                roomNumber = roomNumber,
                counterNumber = hotNumber,
                counterType = CounterTypeEnum.HOT_WATER
            )
            val coldCounter = WaterCounterVO(
                roomNumber = roomNumber,
                counterNumber = coldNumber,
                counterType = CounterTypeEnum.COLD_WATER
            )
            counters.add(hotCounter)
            counters.add(coldCounter)
        }
        return counters
    }

    private fun getStringValue(cell: Cell): String =  when (cell.cellType) {
        CellType.NUMERIC -> cell.numericCellValue.toBigDecimal().setScale(0).toString()
        CellType.STRING -> cell.stringCellValue.trim()
        else -> "UNKNOWN"
    }

}