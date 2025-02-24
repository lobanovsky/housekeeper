package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.model.entity.Debt
import java.io.File
import java.io.FileOutputStream


fun toExcelDebt(debts: List<Debt>) {
    val workBook = XSSFWorkbook()
    createDebtSheet(workBook, sheetName = "Долги. Кап.ремонт", debts)
    FileOutputStream(File("/Users/evgeny/Projects/tsn/housekeeper/etc/debt/Кап.ремонт.xlsx")).use { fos ->
        workBook.write(fos)
    }
}

fun createDebtSheet(workBook: Workbook, sheetName: String, debts: List<Debt>) {
    val sheet = workBook.createSheet(sheetName)

    val headers = listOf(
        "№",
        "Помещение",
        "Лицевой счёт",
        "Сумма",
        "ФИО",
        "Площадь",
    )
    for (columnIndex in 0 until headers.size + 1) sheet.setColumnWidth(columnIndex, 256 * 25)
    val header: Row = sheet.createRow(0)
    for (index in headers.indices) {
        header.createCell(index).setCellValue(headers[index])
    }
    for (i in debts.indices) {
        val index = i + 1
        val row: Row = sheet.createRow(index)
        row.createCell(0).setCellValue(index.toString())
        row.createCell(1).setCellValue(debts[i].room)
        row.createCell(2).setCellValue(debts[i].account)
        row.createCell(3).setCellValue(debts[i].sum.toDouble())
        row.createCell(4).setCellValue(debts[i].fullName)
        row.createCell(5).setCellValue(debts[i].square.toDouble())
    }
}
