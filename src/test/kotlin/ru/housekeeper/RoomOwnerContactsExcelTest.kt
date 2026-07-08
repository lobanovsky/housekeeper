package ru.housekeeper

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.excel.toExcelRoomOwnerContacts
import ru.housekeeper.model.dto.OwnerContactPhoneResponse
import ru.housekeeper.model.dto.RoomOwnerContactsResponse
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import kotlin.test.assertEquals

class RoomOwnerContactsExcelTest {

    @Test
    fun toExcelRoomOwnerContactsCreatesRowsPerPhoneAndKeepsRowsWithoutPhones() {
        val bytes = toExcelRoomOwnerContacts(
            listOf(
                contact(
                    roomNumber = "101",
                    ownerFullName = "Owner One",
                    phones = listOf(
                        OwnerContactPhoneResponse("79990000001", "Resident One"),
                        OwnerContactPhoneResponse("79990000002", null),
                    ),
                ),
                contact(roomNumber = "55", ownerFullName = "Owner Two", phones = emptyList()),
            )
        )

        XSSFWorkbook(ByteArrayInputStream(bytes)).use { workbook ->
            val sheet = workbook.getSheet("Контакты собственников")
            assertEquals("Телефон", sheet.getRow(0).getCell(6).stringCellValue)
            assertEquals("101", sheet.getRow(1).getCell(2).stringCellValue)
            assertEquals("79990000001", sheet.getRow(1).getCell(6).stringCellValue)
            assertEquals("Resident One", sheet.getRow(1).getCell(7).stringCellValue)
            assertEquals("79990000002", sheet.getRow(2).getCell(6).stringCellValue)
            assertEquals("", sheet.getRow(2).getCell(7).stringCellValue)
            assertEquals("55", sheet.getRow(3).getCell(2).stringCellValue)
            assertEquals("", sheet.getRow(3).getCell(6).stringCellValue)
        }
    }

    private fun contact(
        roomNumber: String,
        ownerFullName: String,
        phones: List<OwnerContactPhoneResponse>,
    ) = RoomOwnerContactsResponse(
        roomId = 1,
        buildingId = 1,
        roomNumber = roomNumber,
        roomType = RoomTypeEnum.FLAT,
        roomTypeDescription = RoomTypeEnum.FLAT.description,
        account = "0000500101",
        square = BigDecimal.TEN,
        ownerId = 10,
        ownerFullName = ownerFullName,
        phones = phones,
    )
}
