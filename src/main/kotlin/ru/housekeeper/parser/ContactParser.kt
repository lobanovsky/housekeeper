package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.ContactVO
import ru.housekeeper.utils.logger

class ContactParser(private val file: MultipartFile) {

    fun parse(): List<ContactVO> {
        logger().info("Start parsing registry file ${file.originalFilename}")
        val workbook = WorkbookFactory.create(file.inputStream)

        val contacts = mutableListOf<ContactVO>()
        //gate
        val gateSheet = workbook.getSheetAt(2)
        logger().info("Parsing gate sheet: ${gateSheet.sheetName}")
        val flats = parser(gateSheet)
        logger().info("Parsed ${flats.size} rooms")
        contacts.addAll(flats)
        //garage
        val garageSheet = workbook.getSheet("garage")
        logger().info("Parsing garage sheet: ${garageSheet.sheetName}")
        val garages = parser(garageSheet, RoomTypeEnum.GARAGE)
        logger().info("Parsed ${garages.size} rooms")
        contacts.addAll(garages)

        return contacts
    }

    private fun parser(sheet: Sheet, roomType: RoomTypeEnum = RoomTypeEnum.FLAT): List<ContactVO> {
        val blockNum = 1
        val tenantNum = 2
        val fullNameNum = 3
        val labelNum = 4
        val phoneNum = 5
        val cardNumberNum = 6
        val carNum = 7
        val emailNum = 8

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val contacts = mutableListOf<ContactVO>()
        val numberOfSkippingRows = 1
        for (i in numberOfSkippingRows..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            val block = row.getCell(blockNum).stringCellValue.trim()
            val tenant = row.getCell(tenantNum).stringCellValue.trim()
            val fullName = row.getCell(fullNameNum).stringCellValue.trim()
            val label = row.getCell(labelNum).stringCellValue.trim()
            val phone = row.getCell(phoneNum).stringCellValue.trim()
            val cardNumber = row.getCell(cardNumberNum).stringCellValue.trim()
            val car = row.getCell(carNum).stringCellValue.trim()
            val email = row.getCell(emailNum).stringCellValue.trim()

            logger().info("Parsed row: $block, $tenant, $fullName, $label, $phone, $cardNumber, $car, $email")

            contacts.add(
                ContactVO(
                    block = block == "1",
                    tenant = tenant == "1",
                    fullName = fullName.trim(),
                    label = label.trim().split("-")[0],
                    phone = phone.trim(),
                    carNumber = cardNumber.trim(),
                    car = car.trim(),
                    email = email.trim(),
                    roomType = if (isOffice(label)) RoomTypeEnum.OFFICE else roomType
                )
            )
        }
        return contacts
    }

    private fun isOffice(label: String): Boolean {
        val split = label.trim().split("-")
        if (split.size < 2) return false
        return split[1].startsWith("office")
    }
}