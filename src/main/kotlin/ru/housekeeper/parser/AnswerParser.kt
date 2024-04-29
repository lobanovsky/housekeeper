package ru.housekeeper.parser

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.AnswerEnum
import ru.housekeeper.model.dto.AnswerVO
import ru.housekeeper.utils.NUMBER_OF_QUESTIONS
import ru.housekeeper.utils.logger

class AnswerParser(private val file: MultipartFile) {

    fun parse():List<AnswerVO> {
        logger().info("Start parsing answers file ${file.originalFilename}")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheet("Ответы на вопросы")
        logger().info("Parsing sheet: ${sheet.sheetName}")
        val answers = sheetParser(sheet)
        logger().info("Parsed ${answers.size} answers")
        return answers
    }

    private fun sheetParser(sheet: Sheet): List<AnswerVO> {
        val idNum = 1
        val fullNameNum = 2
        val firstQuestionNum = 6


        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val answers = mutableListOf<AnswerVO>()
        val numberOfSkippingRows = 1
        for (i in numberOfSkippingRows..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            val id = row.getCell(idNum).stringCellValue
            val fullName = row.getCell(fullNameNum).stringCellValue.trim()

            val result = mutableListOf<AnswerEnum>()
            for (j in 0 until NUMBER_OF_QUESTIONS) {
                val cell = row.getCell(j + firstQuestionNum) ?: break
                val answer = when (cell.cellType) {
                    CellType.NUMERIC -> cell.numericCellValue.toInt().toString()
                    CellType.STRING -> cell.stringCellValue
                    else -> ""
                }
                if (answer.isBlank()) break
                result.add(AnswerEnum.determine(answer))
            }
            if (result.isEmpty()) continue
            answers.add(
                AnswerVO(
                    decisionId = id.toLong(),
                    fullName = fullName,
                    answers = result
                )
            )
        }
        return answers
    }
}