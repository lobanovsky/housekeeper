package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.payment.PaymentCounterparty
import ru.housekeeper.model.dto.payment.PaymentVO
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PaymentParser(private val file: MultipartFile) {

    fun parse(): List<PaymentVO> {
        logger().info("Start parsing payments file ${file.originalFilename}")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheetIterator = workbook.sheetIterator()
        val payments = mutableListOf<PaymentVO>()
        sheetIterator.forEach { sheet ->
            logger().info("Sheet: ${sheet.sheetName}")
            payments.addAll(sheetParser(sheet))
        }
        return payments
    }

    private fun sheetParser(sheet: Sheet): List<PaymentVO> {
        val payerNum = 4
        val recipientNum = 8
        val outgoingSumNum = 9
        val incomingSumNum = 13
        val docNumberNum = 14
        val voNum = 16
        val bikAndNameNum = 17
        val findDate = findMarker("Дата проводки", sheet)
        val findPurpose = findMarker("Назначение платежа", sheet)

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val payments = mutableListOf<PaymentVO>()
        for (i in findDate.first..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            if (row.getCell(voNum).stringCellValue.trim().isEmpty()) {
                logger().info("$i: Checking the VO cell for emtpy: VO is null, continue")
                continue
            }
            val payer = counterpartyParser(row.getCell(payerNum).stringCellValue.trim())
            val recipient = counterpartyParser(row.getCell(recipientNum).stringCellValue.trim())
            val (bik, bankName) = bikAndNameParser(row.getCell(bikAndNameNum).stringCellValue.trim())

            val cellType = row.getCell(findDate.second).cellType

            val date = when (cellType) {
                CellType.NUMERIC -> row.getCell(findDate.second).localDateTimeCellValue
                CellType.STRING -> LocalDate.parse(
                    row.getCell(findDate.second).stringCellValue.toString().trim(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                ).atStartOfDay()

                else -> throw IllegalArgumentException("Unknown cell type")
            }
            val docNumber = row.getCell(docNumberNum).stringCellValue.trim()
            val vo = row.getCell(voNum).stringCellValue.trim()
            val outgoingSum = getSumOrNull(row.getCell(outgoingSumNum))
            val incomingSum = getSumOrNull(row.getCell(incomingSumNum))
            val purpose = row.getCell(findPurpose.second).stringCellValue.trim()

            logger().info("#$i: date=$date, num=$docNumber, out=$outgoingSum, in=$incomingSum, purpose=$purpose")

            payments.add(
                PaymentVO(
                    uuid = "${date.toLocalDate()} $docNumber ${sum(outgoingSum, incomingSum)}",
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
        return if (value != 0.0) BigDecimal.valueOf(value).setScale(2) else null
    }

    //1) БИК 042202603, ВОЛГО-ВЯТСКИЙ БАНК ПАО СБЕРБАНК Г. Нижний Новгород
    //2) БИК 044525232 ПАО "МТС-Банк", г.Москва
    //1) bik = 042202603, bankName = ВОЛГО-ВЯТСКИЙ БАНК ПАО СБЕРБАНК Г. Нижний Новгород
    //2) bik = 044525232, bankName = ПАО "МТС-Банк"
    private fun bikAndNameParser(bikAndName: String): Pair<String?, String?> {
        if (bikAndName.isBlank()) return Pair(null, null)
        val split = bikAndName.split(" ")
        val bik = split[1].replace(",", "").trim()
        val name = split.subList(2, split.size).joinToString(" ")
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

    private fun findMarker(marker: String, sheet: Sheet): Pair<Int, Int> {
        var i = 0;
        var j = 0;
        for (row in sheet) {
            i++
            for (cell in row) {
                j++
                if (cell.cellType != CellType.STRING) continue
                if (cell.stringCellValue.isBlank()) continue
                if (cell.stringCellValue.contains(marker)) {
                    return Pair(i + 1, j - 1)
                }
            }
            j = 0
        }
        throw IllegalArgumentException("Marker not found: $marker")
    }
}