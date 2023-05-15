package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.model.entity.Decision
import ru.housekeeper.utils.NUMBER_OF_QUESTIONS
import java.io.ByteArrayOutputStream


fun toExcelForTemplate(decisions: List<Decision>): ByteArray {
    val workBook = XSSFWorkbook()
    createAnswerSheet(workBook, "Ответы на вопросы", decisions.sortedBy { it.numbersOfRooms })
    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}

fun createAnswerSheet(workBook: Workbook, sheetName: String, decisions: List<Decision>) {
    val sheet = workBook.createSheet(sheetName)

    val headers = mutableListOf(
        "№",
        "ID",
        "ФИО",
        "Недвижимость",
        "Общая площадь",
        "Доля",
    )
    for (i in 1..NUMBER_OF_QUESTIONS) {
        headers.add("Вопрос $i")
    }

    for (columnIndex in 0 until headers.size + 1) sheet.setColumnWidth(columnIndex, 256 * 25)
    val header: Row = sheet.createRow(0)
    for (index in headers.indices) {
        header.createCell(index).setCellValue(headers[index])
    }
    for (i in decisions.indices) {
        val index = i + 1
        val row: Row = sheet.createRow(index)
        row.createCell(0).setCellValue(index.toString())
        row.createCell(1).setCellValue(decisions[i].id.toString())
        row.createCell(2).setCellValue(decisions[i].fullName)
        row.createCell(3).setCellValue(decisions[i].numbersOfRooms)
        row.createCell(4).setCellValue(decisions[i].square.toDouble())
        row.createCell(5).setCellValue(decisions[i].percentage.toDouble())
    }
}


