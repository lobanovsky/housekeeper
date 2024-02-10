package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.registry.SberVersionEnum
import ru.housekeeper.model.dto.payment.PaymentCounterparty
import ru.housekeeper.model.dto.payment.PaymentVO
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PaymentParser(private val file: MultipartFile) {

    private fun getVersion(sheet: Sheet): SberVersionEnum {
        val row = sheet.getRow(1)
        val version = row.getCell(5).stringCellValue
        if (version.startsWith("СберБизнес 41.")) return SberVersionEnum.V1
        if (version.startsWith("СберБизнес. 03")) return SberVersionEnum.V2
        return SberVersionEnum.UNKNOWN
    }

    fun parse(): List<PaymentVO> {
        logger().info("Start parsing payments file ${file.originalFilename}")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheetIterator = workbook.sheetIterator()
        val payments = mutableListOf<PaymentVO>()
        sheetIterator.forEach { sheet ->
            logger().info("Sheet: ${sheet.sheetName}")
            val version = getVersion(sheet)
            logger().info("Parse version $version")
            payments.addAll(sheetParser(sheet, version))
        }
        return payments
    }

    private fun sheetParser(sheet: Sheet, version: SberVersionEnum): List<PaymentVO> {
        val dateNum = if (version == SberVersionEnum.V1) 1 else 2
        val payerNum = 4
        val recipientNum = 8
        val outgoingSumNum = 9
        val incomingSumNum = 13
        val docNumberNum = 14
        val voNum = 16
        val binAndNameNum = 17
        val purposeNum = if (version == SberVersionEnum.V1) 20 else 19

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val payments = mutableListOf<PaymentVO>()
        val numberOfSkippingRows = if (version == SberVersionEnum.V1) 11 else 16
        for (i in numberOfSkippingRows..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            if (row.getCell(voNum).stringCellValue.trim().isEmpty()) {
                logger().info("$i: Checking the VO cell for emtpy: VO is null, continue")
                continue
            }
            val payer = counterpartyParser(row.getCell(payerNum).stringCellValue.trim())
            val recipient = counterpartyParser(row.getCell(recipientNum).stringCellValue.trim())
            val (bik, bankName) = when (version) {
                SberVersionEnum.V1 -> bikAndNameParser(row.getCell(binAndNameNum).stringCellValue.trim())
                SberVersionEnum.V2 -> bikAndNameParserV2(row.getCell(binAndNameNum).stringCellValue.trim())
                SberVersionEnum.UNKNOWN -> Pair("", "")
            }
            val date = when (version) {
                SberVersionEnum.V1 -> row.getCell(dateNum).localDateTimeCellValue
                SberVersionEnum.V2 -> LocalDate.parse(
                    row.getCell(dateNum).stringCellValue.toString().trim(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                ).atStartOfDay()

                SberVersionEnum.UNKNOWN -> LocalDateTime.now()
            }
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

    //БИК 044525232 ПАО "МТС-Банк", г.Москва
    fun bikAndNameParserV2(bikAndName: String): Pair<String, String?> {
        val split = bikAndName.split(" ", limit = 3)
        val bik = split[1]
        val name = if (split.size > 2) split[2] else null
        return Pair(bik, name)
    }

    //30233810642000600001
    //7707083893
    //ПАО СБЕРБАНК//Тверетнев Петр Петрович//1541619130338//115580,РОССИЯ,МОСКВА Г,Г МОСКВА,УЛ ШИПИЛОВСКАЯ,Д 55 КВ 81//
    private fun counterpartyParser(payer: String): PaymentCounterparty {
        val split = payer.split("\n")
        val oneLine = 1
        val accountAndNameOnly = 2
        return when (split.size) {
            oneLine -> PaymentCounterparty(account = payer.substring(0, 20), name = payer.substring(20))
            accountAndNameOnly -> PaymentCounterparty(account = split[0], name = split[1])
            else -> PaymentCounterparty(account = split[0], inn = split[1], name = split[2])
        }
    }
}