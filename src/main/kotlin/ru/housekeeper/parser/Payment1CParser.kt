package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.payment.PaymentCounterparty
import ru.housekeeper.model.dto.payment.PaymentVO
import ru.housekeeper.utils.logger
import java.io.InputStream
import java.math.BigDecimal

//1C parser
//UTF-8
//v 1.03
//txt
class Payment1CParser(private val file: MultipartFile) {

    fun parse(): List<PaymentVO> {
        logger().info("Start parsing payments 1C txt file ${file.originalFilename}")
        val payments = mutableListOf<PaymentVO>()
        val lines = readAllLines(file.inputStream)
        payments.addAll(parser(lines))
        return payments
    }

    private fun readAllLines(inputStream: InputStream): List<String> {
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().useLines { lines.addAll(it.toList()) }
        return lines
    }

    private fun parser(lines: List<String>): List<PaymentVO> {

        for (line in lines) {
            if (line != "СекцияДокумент=Платежное поручение") continue
            println(line)
            val parts = line.split('=')
            val key = parts[0]
            val value = parts[1]

            val number: String
            val date: String
            val outgoingSum: BigDecimal
            val incomingSum: BigDecimal
            val payer: PaymentCounterparty
            val recipient: PaymentCounterparty
            when (key) {
                "Номер" -> number = value
                "Дата" -> date = value
                "Сумма" -> incomingSum = BigDecimal(value)
            }
            println("$key = $value")

        }
        return emptyList()
    }

    private fun sheetParser(sheet: Sheet): List<PaymentVO> {
        val dateNum = 1
        val payerNum = 4
        val recipientNum = 8
        val outgoingSumNum = 9
        val incomingSumNum = 13
        val docNumberNum = 14
        val voNum = 16
        val binAndNameNum = 17
        val purposeNum = 20

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val payments = mutableListOf<PaymentVO>()
        val numberOfSkippingRows = 11
        for (i in numberOfSkippingRows..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            if (row.getCell(dateNum).cellType != CellType.NUMERIC) {
                logger().info("$i: Checking cell: Date is null, continue")
                continue
            }

            val payer = counterpartyParser(row.getCell(payerNum).stringCellValue.trim())
            val recipient = counterpartyParser(row.getCell(recipientNum).stringCellValue.trim())
            val (bik, bankName) = bikAndNameParser(row.getCell(binAndNameNum).stringCellValue.trim())
            val date = row.getCell(dateNum).localDateTimeCellValue
            val docNumber = row.getCell(docNumberNum).stringCellValue.trim()
            val vo = row.getCell(voNum).stringCellValue.trim()
            val outgoingSum = getSumOrNull(row.getCell(outgoingSumNum))
            val incomingSum = getSumOrNull(row.getCell(incomingSumNum))
            val purpose = row.getCell(purposeNum).stringCellValue.trim()

            logger().info("#$i: date=$date, num=$docNumber, out=$outgoingSum, in=$incomingSum, purpose=$purpose")

            payments.add(
                PaymentVO(
                    uuid = "$date $docNumber ${sum(outgoingSum, incomingSum)}",
                    date = date,
                    fromAccount = payer.account,
                    fromInn = payer.inn,
                    fromName = payer.name,
                    toAccount = recipient.account,
                    toInn = recipient.inn,
                    toName = recipient.name,
                    outgoingSum = outgoingSum,
                    incomingSum = incomingSum,
                    docNumber = docNumber,
                    vo = vo,
                    bik = bik,
                    bankName = bankName,
                    purpose = purpose,
                )
            )
        }
        return payments
    }

    private fun sum(a: BigDecimal?, b: BigDecimal?): BigDecimal {
        return (a ?: BigDecimal.ZERO) + (b ?: BigDecimal.ZERO)
    }

    private fun getSumOrNull(cell: Cell): BigDecimal? {
        if (cell.cellType != CellType.NUMERIC) return null
        val value = cell.numericCellValue
        return if (value != 0.0) BigDecimal.valueOf(value) else null
    }

    //БИК 042202603, ВОЛГО-ВЯТСКИЙ БАНК ПАО СБЕРБАНК Г. Нижний Новгород
    private fun bikAndNameParser(bikAndName: String): Pair<String, String> {
        val split = bikAndName.split(",")
        val bik = split[0].substring(4)
        val name = split[1].substring(1)
        return Pair(bik, name)
    }

    //30233810642000600001
    //7707083893
    //ПАО СБЕРБАНК//Тверетнев Петр Петрович//1541619130338//115580,РОССИЯ,МОСКВА Г,Г МОСКВА,УЛ ШИПИЛОВСКАЯ,Д 55 КВ 81//
    private fun counterpartyParser(payer: String): PaymentCounterparty {
        val split = payer.split("\n")
        val accountAndNameOnly = 2
        return when (split.size) {
            accountAndNameOnly -> PaymentCounterparty(
                account = split[0], name = split[1]
            )

            else -> PaymentCounterparty(
                account = split[0], inn = split[1], name = split[2]
            )
        }
    }
}