package ru.tsn.housekeeper.parser

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartFile
import ru.tsn.housekeeper.enums.AnswerEnum
import ru.tsn.housekeeper.model.dto.AnswerVO
import ru.tsn.housekeeper.utils.NUMBER_OF_QUESTIONS
import ru.tsn.housekeeper.utils.logger

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
                val answer = when (row.getCell(j + firstQuestionNum).cellType) {
                    CellType.NUMERIC -> row.getCell(j + firstQuestionNum).numericCellValue.toInt().toString()
                    CellType.STRING -> row.getCell(j + firstQuestionNum).stringCellValue
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