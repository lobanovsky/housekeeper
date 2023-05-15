package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.model.dto.AnnualPaymentVO
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.Month


fun toExcelAnnualPayments(annualPayment: AnnualPaymentVO): ByteArray {
    val workBook = XSSFWorkbook()
    val mainSheet = workBook.createSheet(annualPayment.year.toString())

    val yearRow = mainSheet.createRow(0)
    yearRow.createCell(0).setCellValue("Год")
    yearRow.createCell(1).setCellValue("${annualPayment.year}")

    val taxFreeSumRow = mainSheet.createRow(1)
    taxFreeSumRow.createCell(0).setCellValue("ЖКХ платежи ")
    taxFreeSumRow.createCell(1).setCellValue(annualPayment.taxFreeSum.toString().replace(".",","))

    val taxableSumRow = mainSheet.createRow(2)
    taxableSumRow.createCell(0).setCellValue("Поступления")
    taxableSumRow.createCell(1).setCellValue(annualPayment.taxableSum.toString().replace(".",","))

    val depositsSumRow = mainSheet.createRow(3)
    depositsSumRow.createCell(0).setCellValue("Депозиты")
    depositsSumRow.createCell(1).setCellValue((annualPayment.depositSum ?: BigDecimal.ZERO).toString().replace(".",","))

    val totalSumRow = mainSheet.createRow(4)
    totalSumRow.createCell(0).setCellValue("Общая сумма")
    totalSumRow.createCell(1).setCellValue(annualPayment.totalSum.toString().replace(".",","))

    val headerRow = mainSheet.createRow(5)
    headerRow.createCell(1).setCellValue("ЖКХ платежи (шт.)")
    headerRow.createCell(2).setCellValue("ЖКХ платежи (сумма)")
    headerRow.createCell(4).setCellValue("Поступления (шт.)")
    headerRow.createCell(5).setCellValue("Поступления (сумма)")
    headerRow.createCell(7).setCellValue("Депозиты (шт.)")
    headerRow.createCell(8).setCellValue("Депозиты (сумма)")

    for (columnIndex in 0 until 10) mainSheet.setColumnWidth(columnIndex, 256 * 20)

    val startIndex = 6
    var index = startIndex
    for (month in Month.values()) {
        val row: Row = mainSheet.createRow(index++)
        row.createCell(0).setCellValue(month.name)
        row.createCell(1).setCellValue(annualPayment.taxFreePaymentsByMonths.firstOrNull { it.month == month }?.size?.toDouble() ?: 0.0)
        row.createCell(2).setCellValue(annualPayment.taxFreePaymentsByMonths.firstOrNull { it.month == month }?.totalSum?.toDouble() ?: 0.0)
        row.createCell(4).setCellValue(annualPayment.taxablePaymentsByMonths.firstOrNull { it.month == month }?.size?.toDouble() ?: 0.0)
        row.createCell(5).setCellValue(annualPayment.taxablePaymentsByMonths.firstOrNull { it.month == month }?.totalSum?.toDouble() ?: 0.0)
        row.createCell(7).setCellValue(annualPayment.depositsPaymentsByMonths.firstOrNull { it.month == month }?.size?.toDouble() ?: 0.0)
        row.createCell(8).setCellValue(annualPayment.depositsPaymentsByMonths.firstOrNull { it.month == month }?.totalSum?.toDouble() ?: 0.0)
    }

    createPaymentSheet(workBook,"ЖКХ платежи", annualPayment.taxFreePaymentsByMonths.flatMap { it.payments } )
    createPaymentSheet(workBook,"Поступления", annualPayment.taxablePaymentsByMonths.flatMap { it.payments } )
    createPaymentSheet(workBook,"Депозиты", annualPayment.depositsPaymentsByMonths.flatMap { it.payments } )

    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}