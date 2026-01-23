package ru.housekeeper.experiment

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

/**
 * Парсер для преобразования контактов в доступные зоны
 */

fun main1() {
    val workbook = WorkbookFactory.create(File("/Users/evgeny/Projects/tsn/housekeeper/etc/contacts/gates.xlsx"))
    val gateSheet = workbook.getSheet("gate")
    val parkingSheet = workbook.getSheet("parking")

    for (i in 15..gateSheet.lastRowNum) {
        val row = gateSheet.getRow(i)
        val name = row.getCell(4).stringCellValue.trim()
        val flat = row.getCell(5).stringCellValue
        val phone = row.getCell(6).stringCellValue

        if (phone.isNotBlank()) println("${flat.split("-")[0]}, $phone, name: $name")
    }

    for (i in 5..parkingSheet.lastRowNum) {
        val row = parkingSheet.getRow(i)
        val name = row.getCell(4).stringCellValue.trim()
        val flat = row.getCell(5).stringCellValue
        val phone = row.getCell(6).stringCellValue

        if (phone.isNotBlank()) println("${flat.split("-")[0]}, $phone, name: $name")
    }
}