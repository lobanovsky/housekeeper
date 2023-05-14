package ru.tsn.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.tsn.housekeeper.model.dto.PaymentVO
import ru.tsn.housekeeper.utils.incomingSum
import ru.tsn.housekeeper.utils.yyyyMMddDateFormat
import java.io.ByteArrayOutputStream


fun toExcelPayments(payments: List<PaymentVO>): ByteArray {
    val companyName = payments[0].fromName
    val workBook = XSSFWorkbook()
    createPaymentSheet(workBook, companyName, payments)
    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}

fun createPaymentSheet(workBook: Workbook, sheetName: String, payments: List<PaymentVO>) {
    val sheet = workBook.createSheet(sheetName)

    val headers = listOf(
        "№",
        "Дата платежа",
        "Сумма",
        "Назначение платежа",
        "От ИНН",
        "От Наименование",
        "Кому ИНН",
        "Кому Наименование",
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
        row.createCell(1).setCellValue(payments[i].date.format(yyyyMMddDateFormat()) ?: "")
        row.createCell(sumIndex).setCellValue(payments[i].incomingSum?.toDouble() ?: 0.0)
        row.createCell(3).setCellValue(payments[i].purpose)
        row.createCell(4).setCellValue(payments[i].fromInn)
        row.createCell(5).setCellValue(payments[i].fromName)
        row.createCell(6).setCellValue(payments[i].toInn)
        row.createCell(7).setCellValue(payments[i].toName)
    }
    val totalSum = payments.incomingSum()
    sheet.createRow(payments.size + 1).createCell(sumIndex).setCellValue(totalSum.toDouble())
}
