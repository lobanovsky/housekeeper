package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.model.dto.payment.GroupOfPayment
import ru.housekeeper.model.entity.payment.OutgoingPayment
import ru.housekeeper.utils.sum
import ru.housekeeper.utils.yyyyMMddDateFormat
import java.io.ByteArrayOutputStream


fun toExcelGroupOfPayments(payments: List<GroupOfPayment>): ByteArray {
    val workBook = XSSFWorkbook()
    createGroupOfPaymentSheet(workBook, sheetName = "Платежи", payments)

    for (payment in payments) {
        val sheetName = payment.counterparty.name
        createDetailSheet(workBook, sheetName, payment.payments)
    }

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
        row.createCell(1).setCellValue(payments[i].counterparty.name)
        row.createCell(2).setCellValue(payments[i].total.toDouble())
    }
    val totalSum = payments.sumOf { it.total }
    sheet.createRow(payments.size + 1).createCell(sumIndex).setCellValue(totalSum.toDouble())
}

fun createDetailSheet(workBook: Workbook, sheetName: String, payments: List<OutgoingPayment>) {
    val sheet = workBook.createSheet(sheetName)

    val headers = listOf(
        "№",
        "Дата платежа",
        "Сумма",
        "Кому ИНН",
        "Кому Наименование",
        "Назначение платежа",
        "От ИНН",
        "От Наименование",
        "Счёт поступления"
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
        row.createCell(sumIndex).setCellValue(payments[i].sum?.toDouble() ?: 0.0)
        row.createCell(3).setCellValue(payments[i].toInn)
        row.createCell(4).setCellValue(payments[i].toName)
        row.createCell(5).setCellValue(payments[i].purpose)
        row.createCell(6).setCellValue(payments[i].fromInn)
        row.createCell(7).setCellValue(payments[i].fromName)
        row.createCell(8).setCellValue(payments[i].toAccount)
    }
    val totalSum = payments.sum()
    sheet.createRow(payments.size + 1).createCell(sumIndex).setCellValue(totalSum.toDouble())
}
