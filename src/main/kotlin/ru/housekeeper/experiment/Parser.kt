package ru.housekeeper.experiment

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

data class Payment(
    val flat: String,
    val name: String,
    val money: BigDecimal
)

data class StartPayment(
    val flat: String,
    val account: String,
    val money: BigDecimal
)

data class ResultPayment(
    val flat: String,
    val account: String,
    val start: BigDecimal,
    val end: BigDecimal,
    val diff: BigDecimal
)

/**
 * Формирование списка платежей в виде kotlin-кода для платежа по Капитальному ремонту
 */
fun main4() {
    val workbook = WorkbookFactory.create(File("/Users/evgeny/Projects/tsn/housekeeper/etc/pay/flat_53471_48.xlsx"))
    val sheet = workbook.getSheet("Лист1")
    val secondSheet = workbook.getSheet("2")

    val payments = mutableMapOf<String, Payment>()
    var sum = BigDecimal.ZERO
    for (i in 0..sheet.lastRowNum) {
        val row = sheet.getRow(i)
        val flat = row.getCell(0).stringCellValue
        val name = row.getCell(1).stringCellValue
        val money = BigDecimal(row.getCell(10).numericCellValue).setScale(2, RoundingMode.HALF_UP)
        sum += money
        payments[flat] = Payment(flat, name, money)
    }
    //pretty print payments
    payments.forEach { println(it) }
    println("Sum: $sum")

    val startPayments = mutableMapOf<String, StartPayment>()
    var secondSum = BigDecimal.ZERO
    for (i in 0..secondSheet.lastRowNum) {
        val row = secondSheet.getRow(i)
        val flat = (i + 1).toString()
        val account = row.getCell(1).stringCellValue
        val money = BigDecimal(row.getCell(0).numericCellValue).setScale(2, RoundingMode.HALF_UP)
        secondSum += money
        startPayments[flat] = StartPayment(flat, account, money)
    }
    //pretty print payments
    startPayments.forEach { println(it) }
    println("Sum: $secondSum")

    //make result payments
    val resultPayments = mutableListOf<ResultPayment>()
    for (key in startPayments.keys) {
        val startPayment = startPayments[key]!!
        val payment = payments[key] ?: Payment(key, "unknown", BigDecimal.ZERO)
        resultPayments.add(
            ResultPayment(
                flat = key,
                account = startPayment.account,
                start = startPayment.money,
                end = payment.money,
                diff = startPayment.money.subtract(payment.money)
            )
        )
    }
    //pretty print result payments
    resultPayments.forEach { println(it) }
    val sumOf = resultPayments.sumOf { it.diff }
    println("Sum of: $sumOf")

    //val p01 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000004165", fromName = "Безмен Виктор Георгиевич", sum = BigDecimal("4338.61"))
    var total = BigDecimal.ZERO
    val variables = mutableListOf<String>()
    resultPayments.forEach {
        if (it.diff != BigDecimal("0.00")) {
            total += it.diff
            variables.add("p_f_${it.flat}")
            val string =
                "val p_f_${it.flat} = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = \"${it.account}\", fromName = \"${it.account}\", sum = BigDecimal(\"${it.diff}\"))"
            println(string)

        }
    }
    println("val p_f53471_49 = listOf(${variables.joinToString(", ")})")
    println("Sum: $total")

//    //make map of payments from 1 to 144, use payments map, if except key, then 0
//    val paymentsMap = mutableMapOf<Int, BigDecimal>()
//    for (i in 1..144) {
//        paymentsMap[i] = payments[i.toString()] ?: BigDecimal.ZERO
//    }
//    println(paymentsMap)
//
//    //make excel with payments
//    val workBook = XSSFWorkbook()
//    val mainSheet = workBook.createSheet("payments")
//    for (key in paymentsMap.keys) {
//        val row = mainSheet.createRow(key)
//        row.createCell(0).setCellValue(key.toString())
//        row.createCell(1).setCellValue(paymentsMap[key].toString().replace(".", ","))
//    }
//    //and save workbook to file
//    workBook.write(File("/Users/evgeny/Projects/tsn/housekeeper/etc/pay/payments.xlsx").outputStream())
//    workBook.close()
}