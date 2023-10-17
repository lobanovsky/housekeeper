package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.model.dto.payment.GroupOfPayment
import java.io.ByteArrayOutputStream


fun toExcelGroupOfPayments(payments: List<GroupOfPayment>): ByteArray {
    val workBook = XSSFWorkbook()
    createGroupOfPaymentSheet(workBook, sheetName = "Платежи", payments)
    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}

fun createGroupOfPaymentSheet(workBook: Workbook, sheetName: String, payments: List<GroupOfPayment>) {
    val sheet = workBook.createSheet(sheetName)

    val headers = listOf(
        "№",
        "Контрагент",
        "Сумма",
    )
    for (columnIndex in 0 until headers.size + 1) sheet.setColumnWidth(columnIndex, 256 * 25)
    val header: Row = sheet.createRow(0)
    for (index in headers.indices) {
        header.createCell(index).setCellValue(headers[index])
    }
    val sumIndex = 2
    for (i in payments.indices) {
        val index = i + 1
        val row: Row = sheet.createRow(index)
        row.createCell(0).setCellValue(index.toString())
        row.createCell(1).setCellValue(payments[i].counterparty.originalName)
        row.createCell(2).setCellValue(payments[i].total.toDouble())
    }
    val totalSum = payments.sumOf { it.total }
    sheet.createRow(payments.size + 1).createCell(sumIndex).setCellValue(totalSum.toDouble())
}
