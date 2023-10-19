package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.counterparty.CounterpartyVO
import ru.housekeeper.service.makeUUID
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.onlyCyrillicLettersAndNumbers

class CounterpartyParser(private val file: MultipartFile) {

    fun parse(): List<CounterpartyVO> {
        logger().info("Start parsing counterparties file")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)

        val nameNum = 0
        val innNum = 1
        val bankNum = 2
        val bikNum = 3
        val signNum = 4

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val counterpartyInfos = mutableListOf<CounterpartyVO>()
        for (rowNum in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowNum) ?: continue

            val name = row.getCell(nameNum)?.toString() ?: ""
            val inn = row.getCell(innNum)?.toString()?.ifBlank { null }
            val bank = row.getCell(bankNum)?.toString()?.ifBlank { null }
            val bik = row.getCell(bikNum)?.toString()?.ifBlank { null }
            val sign = row.getCell(signNum)?.toString()?.ifBlank { null }
            logger().info("$rowNum) $name $inn $bank $bik $sign")
            if (inn?.isBlank() == true) logger().info("Inn for $name is blank")
            counterpartyInfos.add(
                CounterpartyVO(
                    uuid = makeUUID(inn, name),
                    name = name,
                    inn = inn,
                    bank = bank,
                    bik = bik,
                    sign = sign,
                )
            )
        }
        val filterByName = filterByName(counterpartyInfos)
        val filterByInn = filterByInn(filterByName)
        logger().info("Parsed ${counterpartyInfos.size} -> ${filterByInn.size} unique counterparties")
        return filterByInn
    }

    private fun filterByName(counterpartiesWithDuplicates: List<CounterpartyVO>): List<CounterpartyVO> {
        val items = mutableListOf<CounterpartyVO>()
        val groupByName = counterpartiesWithDuplicates.groupBy { it.name.onlyCyrillicLettersAndNumbers() }
        // groupByName.filter { it.value.size > 1 }.filter { it.value.map{it.inn}.toSet().size > 1 }
        groupByName.forEach { (name, counterpartyInfo) ->
            items.add(counterpartyInfo.first())
        }
        return items
    }

    private fun filterByInn(counterpartiesWithDuplicates: List<CounterpartyVO>): List<CounterpartyVO> {
        val items = mutableListOf<CounterpartyVO>()
        val groupByInn = counterpartiesWithDuplicates.groupBy { it.inn }
        groupByInn.forEach { (inn, counterparties) ->
            if (counterparties.size == 1) {
                items.add(counterparties.first())
            }
            if (counterparties.size > 1) {
                logger().info("Found ${counterparties.size} counterparties with same inn [$inn]")
                val names = counterparties.map { it.name }.toSet()
                if (names.size == 1) {
                    items.add(counterparties.first())
                } else {
                    val distinctByName = counterparties.distinctBy { it.name.onlyCyrillicLettersAndNumbers() }
                    if (excludes(distinctByName)) {
                        items.add(distinctByName.first())
                    } else {
                        distinctByName.forEach {
                            it.uuid = it.name.onlyCyrillicLettersAndNumbers()
                            items.add(it)
                        }
                    }
                }
            }
        }
        return items
    }

    private fun excludes(counterparties: List<CounterpartyVO>): Boolean {
        val partOfNames = listOf("уфк", "казначейств", "оооот", "паомоэк")
        val size = counterparties.size
        var count = 0

        for (name in partOfNames) {
            counterparties.forEach {
                if (it.name.onlyCyrillicLettersAndNumbers().contains(name)) {
                    count++
                }
            }
            if (count == size) {
                return true
            }
            count = 0
        }
        return false
    }

}