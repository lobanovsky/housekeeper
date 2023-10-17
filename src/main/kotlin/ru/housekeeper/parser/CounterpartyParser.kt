package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.CounterpartyInfo
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.simplify

class CounterpartyParser(private val file: MultipartFile) {

    fun parse(): List<CounterpartyInfo> {
        logger().info("Start parsing counterparties file")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)

        val nameNum = 0
        val innNum = 1
        val bankNum = 2
        val bikNum = 3
        val signNum = 4

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val counterpartyInfos = mutableListOf<CounterpartyInfo>()
        for (rowNum in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowNum) ?: continue

            val originalName = row.getCell(nameNum)?.toString() ?: ""
            val name = originalName.simplify()
            val inn = row.getCell(innNum)?.toString() ?: ""
            val bank = row.getCell(bankNum)?.toString() ?: ""
            val bik = row.getCell(bikNum)?.toString() ?: ""
            val sign = row.getCell(signNum)?.toString() ?: ""
            logger().info("$rowNum) $originalName $inn $bank $bik $sign")
            if (inn.isBlank()) {
                logger().info("Inn for $originalName is blank")
            }
            counterpartyInfos.add(
                CounterpartyInfo(
                    uuid = "$name $inn",
                    originalName = originalName,
                    name = name,
                    inn = inn,
                    bank = bank,
                    bik = bik,
                    sign = sign,
                )
            )
        }
        return counterpartyInfos
    }

}