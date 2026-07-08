package ru.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.housekeeper.model.dto.RoomOwnerContactsResponse
import java.io.ByteArrayOutputStream

fun toExcelRoomOwnerContacts(contacts: List<RoomOwnerContactsResponse>): ByteArray {
    val workBook = XSSFWorkbook()
    createRoomOwnerContactsSheet(workBook, contacts)

    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}

private fun createRoomOwnerContactsSheet(workBook: Workbook, contacts: List<RoomOwnerContactsResponse>) {
    val sheet = workBook.createSheet("Контакты собственников")

    val headers = listOf(
        "Дом",
        "Тип помещения",
        "Номер помещения",
        "Лицевой счет",
        "Площадь",
        "ФИО собственника",
        "Телефон",
        "ФИО владельца телефона",
    )
    for (columnIndex in headers.indices) sheet.setColumnWidth(columnIndex, 256 * 25)

    val header: Row = sheet.createRow(0)
    for (index in headers.indices) {
        header.createCell(index).setCellValue(headers[index])
    }

    var rowIndex = 1
    contacts.forEach { contact ->
        if (contact.phones.isEmpty()) {
            createRow(sheet.createRow(rowIndex++), contact, null, null)
        } else {
            contact.phones.forEach { phone ->
                createRow(sheet.createRow(rowIndex++), contact, phone.phoneNumber, phone.fullName)
            }
        }
    }
}

private fun createRow(row: Row, contact: RoomOwnerContactsResponse, phoneNumber: String?, phoneOwnerName: String?) {
    row.createCell(0).setCellValue(contact.buildingId.toDouble())
    row.createCell(1).setCellValue(contact.roomTypeDescription)
    row.createCell(2).setCellValue(contact.roomNumber)
    row.createCell(3).setCellValue(contact.account ?: "")
    row.createCell(4).setCellValue(contact.square.toDouble())
    row.createCell(5).setCellValue(contact.ownerFullName)
    row.createCell(6).setCellValue(phoneNumber ?: "")
    row.createCell(7).setCellValue(phoneOwnerName ?: "")
}
