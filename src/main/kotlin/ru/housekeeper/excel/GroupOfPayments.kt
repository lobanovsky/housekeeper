package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.model.dto.payment.GroupOfPayment
import ru.housekeeper.model.filter.OutgoingGropingPaymentsFilter
import ru.housekeeper.utils.yyyyMMddDateFormat
import java.io.ByteArrayOutputStream


fun toExcelGroupOfPayments(payments: List<GroupOfPayment>, filter: OutgoingGropingPaymentsFilter): ByteArray {
    val workBook = XSSFWorkbook()
    createGroupOfPaymentSheet(
        workBook,
        sheetName = "Платежи " +
                "${filter.startDate?.format(yyyyMMddDateFormat())}-${filter.endDate?.format(yyyyMMddDateFormat())}",
        payments
    )

    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}

fun createGroupOfPaymentSheet(workBook: Workbook, sheetName: String, groups: List<GroupOfPayment>) {
    val sheet = workBook.createSheet(sheetName)

    val headers = listOf(
        "№",
        "Контрагент/Дата",
        "Сумма",
        "Назначение платежа",
    )
    for (columnIndex in 0 until headers.size + 1) sheet.setColumnWidth(columnIndex, 256 * 25)
    val header: Row = sheet.createRow(0)
    for (index in headers.indices) {
        header.createCell(index).setCellValue(headers[index])
    }
    val sumIndex = 2
    var index = 1
    for (i in groups.indices) {
        val row: Row = sheet.createRow(index)
        row.createCell(0).setCellValue(index.toString())
        row.createCell(1).setCellValue(groups[i].name)
        row.createCell(sumIndex).setCellValue(groups[i].total.toDouble())
        index++
        var j = 1
        for (payment in groups[i].payments) {
            val detailRow: Row = sheet.createRow(index)
            detailRow.createCell(0).setCellValue(j.toString())
            detailRow.createCell(1).setCellValue(payment.date.format(yyyyMMddDateFormat()) ?: "")
            detailRow.createCell(sumIndex).setCellValue(payment.sum?.toDouble() ?: 0.0)
            detailRow.createCell(3).setCellValue(payment.purpose)
            index++
            j++
        }
        index++
    }
    val totalSum = groups.sumOf { it.total }
    sheet.createRow(index + 1).createCell(sumIndex).setCellValue(totalSum.toDouble())
}
