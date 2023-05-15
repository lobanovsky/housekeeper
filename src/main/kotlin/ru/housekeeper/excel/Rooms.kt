package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.entity.Room
import java.io.ByteArrayOutputStream
import java.math.BigDecimal


fun toExcelRooms(rooms: List<Room>): ByteArray {
    val workBook = XSSFWorkbook()
    val registries = rooms.map { toRegistry(it) }

    val sortedRegistries = mutableListOf<Registry>()
    sortedRegistries.addAll(registries.filter { it.type == RoomTypeEnum.FLAT }.sortedBy { it.numberOfFlat })
    sortedRegistries.addAll(registries.filter { it.type == RoomTypeEnum.OFFICE }.sortedBy { it.numberOfFlat })
    sortedRegistries.addAll(registries.filter { it.type == RoomTypeEnum.GARAGE }.sortedBy { it.numberOfFlat })

    createRoomSheet(workBook, "Реестр собственников", sortedRegistries)
    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}

private fun toRegistry(room: Room): Registry {
    val number = when (room.type) {
        RoomTypeEnum.FLAT -> "кв.${room.number.padStart(3, '0')}"
        RoomTypeEnum.GARAGE -> "мм.${room.number.padStart(3, '0')}"
        RoomTypeEnum.OFFICE -> "оф.${room.number.padStart(3, '0')}"
    }
    return Registry(
        numberOfFlat = room.number.padStart(3, '0'),
        number = number,
        square = room.square,
        type = room.type,
        owner = room.ownerName,
        certificate = room.certificate ?: "",
        percentage = "${room.percentage}",
    )
}

private fun createRoomSheet(workBook: Workbook, sheetName: String, registries: List<Registry>) {
    val sheet = workBook.createSheet(sheetName)

    val mainInfo = sheet.createRow(0)
    mainInfo.createCell(0).setCellValue("17-й проезд Марьиной Рощи, 1")

    val totalSquare = sheet.createRow(1)
    totalSquare.createCell(0).setCellValue("Общая площадь помещений")
    totalSquare.createCell(1).setCellValue("${(registries.sumOf { it.square }).toDouble()} кв.м.")

    val totalPercentage = sheet.createRow(2)
    totalPercentage.createCell(0).setCellValue("Общая доля собственности")
    totalPercentage.createCell(1).setCellValue("100%")

    val roomHeaders = listOf(
        "Номер помещения",
        "Площадь",
        "ФИО собственника",
        "Доля в праве ОИ",
        "Примечание",
    )
    for (columnIndex in 0 until roomHeaders.size + 1) sheet.setColumnWidth(columnIndex, 256 * 25)
    val header: Row = sheet.createRow(4)
    for (index in roomHeaders.indices) {
        header.createCell(index).setCellValue(roomHeaders[index])
    }

    for (i in registries.indices) {
        val index = i + 5
        val row: Row = sheet.createRow(index)
        row.createCell(0).setCellValue(registries[i].number)
        row.createCell(1).setCellValue(registries[i].square.toDouble())
        row.createCell(2).setCellValue(registries[i].owner)
        row.createCell(3).setCellValue(registries[i].percentage)
        row.createCell(4).setCellValue("")
    }
}

data class Registry(
    val numberOfFlat: String,
    val number: String,
    val square: BigDecimal,
    val type: RoomTypeEnum = RoomTypeEnum.FLAT,
    val owner: String,
    val certificate: String,
    val percentage: String,
)

