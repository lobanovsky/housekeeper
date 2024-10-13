package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.model.entity.AreaEntity
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.entity.access.AccessEntity
import ru.housekeeper.service.RoomService
import java.io.ByteArrayOutputStream

fun toExcelAccesses(
    areas: List<AreaEntity>,
    accesses: List<AccessEntity>,
    roomService: RoomService,
): ByteArray {
    val workBook = XSSFWorkbook()
    areas.forEach { area ->
        val filteredAccess = accesses.filter { access -> access.areas.any { innerArea -> innerArea.areaId == area.id } }
        createSheet(workBook, area, filteredAccess, roomService)
    }
    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}

private fun getRoomNumbersForArea(
    area: AreaEntity,
    ownerId: Long,
    roomService: RoomService,
): List<Room> {
    val buildingIds = area.buildingIds
    return roomService.findByBuildingIdsAndOwnerIds(buildingIds ?: emptySet(), ownerId)
}

private fun createSheet(
    workBook: Workbook,
    area: AreaEntity,
    accesses: List<AccessEntity>,
    roomService: RoomService,
) {
    val sheet = workBook.createSheet(area.name)

    val headers = listOf(
        "№ помещения",
        "Заблокирован",
        "Аренда",
        "Телефон",
        "ФИО",
        "Автомобиль",
    )
    for (columnIndex in 0 until headers.size + 1) sheet.setColumnWidth(columnIndex, 256 * 25)
    val header: Row = sheet.createRow(0)
    for (index in headers.indices) {
        header.createCell(index).setCellValue(headers[index])
    }
    val rows = mutableListOf<ru.housekeeper.excel.Row>()
    for (i in accesses.indices) {
        val rooms = getRoomNumbersForArea(area, accesses[i].ownerId, roomService)
        rows.add(
            Row(
                sortedField = rooms.first().number.padStart(3, '0'),
                rooms = rooms.map { it.type.shortDescription + it.number }.joinToString(", "),
                blocked = if (accesses[i].active) "Нет" else "Да",
                rent = if (accesses[i].tenant == true) "Да" else "Нет",
                phone = accesses[i].phoneNumber,
                fullName = accesses[i].phoneLabel ?: "",
                car = accesses[i].cars?.joinToString(", ") { it.plateNumber + (if (it.description?.isNotEmpty() == true) "[" + it.description + "]" else "") }
                    ?: "",
            )
        )
    }
    val sortedRows = rows.sortedBy { it.sortedField }

    for (i in sortedRows.indices) {
        val index = i + 1
        val row: Row = sheet.createRow(index)
        row.createCell(0).setCellValue(sortedRows[i].rooms)
        row.createCell(1).setCellValue(sortedRows[i].blocked)
        row.createCell(2).setCellValue(sortedRows[i].rent)
        row.createCell(3).setCellValue(sortedRows[i].phone)
        row.createCell(4).setCellValue(sortedRows[i].fullName)
        row.createCell(5).setCellValue(sortedRows[i].car)
    }
}

data class Row(
    val sortedField: String,
    val rooms: String,
    val blocked: String,
    val rent: String,
    val phone: String,
    val fullName: String,
    val car: String,
)