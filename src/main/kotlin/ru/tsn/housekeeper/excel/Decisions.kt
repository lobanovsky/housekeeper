package ru.tsn.housekeeper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.tsn.housekeeper.enums.AnswerEnum
import ru.tsn.housekeeper.model.entity.Decision
import ru.tsn.housekeeper.utils.NUMBER_OF_QUESTIONS
import ru.tsn.housekeeper.utils.QUORUM
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode


fun toExcelDecisions(decisions: List<Decision>): ByteArray {
    val workBook = XSSFWorkbook()
    createMainSheet(workBook, "Основная информация", decisions)
    createPercentageSheet(workBook, "Результаты", decisions)
    createVotedSheet(workBook, "Проголосовали", decisions.filter { it.voted }.sortedBy { it.numbersOfRooms })
    createVotedSheet(workBook, "Не проголосовали", decisions.filter { !it.voted }.sortedByDescending { it.percentage })
    val outputStream = ByteArrayOutputStream()
    workBook.write(outputStream)
    workBook.close()
    return outputStream.toByteArray()
}

//counting of voted
private fun createPercentageSheet(workBook: Workbook, sheetName: String, decisions: List<Decision>) {
    val percentageSheet = workBook.createSheet(sheetName)

    val votedDecisions = decisions.filter { it.answers != null }
    val questions = initQuestions()
    for (decision in votedDecisions) {
        for (i in 0 until NUMBER_OF_QUESTIONS) {
            when (decision.answers!![i]) {
                AnswerEnum.YES -> questions[i].incYes(decision.percentage, decision)
                AnswerEnum.NO -> questions[i].incNo(decision.percentage, decision)
                AnswerEnum.UNDEFINED -> questions[i].incUndefined(decision.percentage, decision)
            }
        }
    }

    val headers = percentageSheet.createRow(0)
    headers.createCell(0).setCellValue("Номер вопроса")
    headers.createCell(1).setCellValue("За")
    headers.createCell(2).setCellValue("Против")
    headers.createCell(3).setCellValue("Воздержался")
    headers.createCell(4).setCellValue("Кворум пройден (более $QUORUM%)")

    for (i in questions.indices) {
        val index = i + 1
        val row: Row = percentageSheet.createRow(index)
        row.createCell(0).setCellValue(index.toString())
        val percentageOfYes = questions[i].percentOfYes
        val percentageOfNo = questions[i].percentOfNo
        val percentageOfUndefined = questions[i].percentOfUndefined

        val total = percentageOfYes + percentageOfNo + percentageOfUndefined
        val yes = percentageOfYes.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
        val no = percentageOfNo.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
        val undefined = percentageOfUndefined.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)

        row.createCell(1).setCellValue("$percentageOfYes ($yes)")
        row.createCell(2).setCellValue("$percentageOfNo ($no)")
        row.createCell(3).setCellValue("$percentageOfUndefined ($undefined)")
        row.createCell(4).setCellValue(if (percentageOfYes.toDouble() > QUORUM) "да" else "нет (${BigDecimal.valueOf(QUORUM - percentageOfYes.toDouble()).setScale(2, RoundingMode.HALF_UP)})")
    }

    val row: Row = percentageSheet.createRow(NUMBER_OF_QUESTIONS + 2)
    row.createCell(0).setCellValue("Всего учтено ответов: ${votedDecisions.size}")
}

private fun initQuestions(): List<QuestionResult> {
    val questions = mutableListOf<QuestionResult>()
    for (i in 1..NUMBER_OF_QUESTIONS) {
        questions.add(QuestionResult(i))
    }
    return questions
}

private fun createMainSheet(workBook: Workbook, sheetName: String, decisions: List<Decision>) {
    val mainSheet = workBook.createSheet(sheetName)

    val headers = mainSheet.createRow(0)
    headers.createCell(0).setCellValue("Основанная информация")
    headers.createCell(1).setCellValue("Количество")
    headers.createCell(2).setCellValue("Площадь (м2)")
    headers.createCell(3).setCellValue("Доля (%)")
//    headers.createCell(4).setCellValue("Не хватает до 67% (2/3 голосов)")

    val totalSquare = mainSheet.createRow(1)
    totalSquare.createCell(0).setCellValue("Общая площадь помещений")
    totalSquare.createCell(1).setCellValue((decisions.sumOf { it.square }).toDouble())

    val totalPercentage = mainSheet.createRow(2)
    totalPercentage.createCell(0).setCellValue("Общая доля собственности")
//    totalPercentage.createCell(1).setCellValue((decisions.sumOf { it.percentage }).toDouble())
    totalPercentage.createCell(1).setCellValue(100.00)

    val totalDecision = mainSheet.createRow(3)
    totalDecision.createCell(0).setCellValue("Всего решений")
    totalDecision.createCell(1).setCellValue(decisions.size.toDouble())

    val totalVotedPercentage = decisions.filter { it.voted }.sumOf { it.percentage }.toDouble()
    val totalNotVotedPercentage = decisions.filter { !it.voted }.sumOf { it.percentage }.toDouble()
    val d = 100 - totalVotedPercentage - totalNotVotedPercentage

    val totalVoted = mainSheet.createRow(4)
    totalVoted.createCell(0).setCellValue("Проголосовали")
    totalVoted.createCell(1).setCellValue(decisions.filter { it.voted }.size.toDouble())
    totalVoted.createCell(2).setCellValue(decisions.filter { it.voted }.sumOf { it.square }.toDouble())
    totalVoted.createCell(3).setCellValue(totalVotedPercentage + d)
//    totalVoted.createCell(4).setCellValue(QUORUM - totalVotedPercentage)

    val totalNotVoted = mainSheet.createRow(5)
    totalNotVoted.createCell(0).setCellValue("Не проголосовали")
    totalNotVoted.createCell(1).setCellValue(decisions.filter { !it.voted }.size.toDouble())
    totalNotVoted.createCell(2).setCellValue(decisions.filter { !it.voted }.sumOf { it.square }.toDouble())
    totalNotVoted.createCell(3).setCellValue(totalNotVotedPercentage)
}

private fun createVotedSheet(workBook: Workbook, sheetName: String, decisions: List<Decision>) {
    val sheet = workBook.createSheet(sheetName)

    val headers = listOf(
        "№",
        "ФИО ($sheetName)",
        "Недвижимость",
        "Общая площадь",
        "Доля",
        "Ответы (З-за, П-против, В-воздержался)"
    )
    for (columnIndex in 0 until headers.size + 1) sheet.setColumnWidth(columnIndex, 256 * 25)
    val header: Row = sheet.createRow(0)
    for (index in headers.indices) {
        header.createCell(index).setCellValue(headers[index])
    }
    for (i in decisions.indices) {
        val index = i + 1
        val row: Row = sheet.createRow(index)
        row.createCell(0).setCellValue(index.toString())
        row.createCell(1).setCellValue(decisions[i].fullName)
        row.createCell(2).setCellValue(decisions[i].numbersOfRooms)
        row.createCell(3).setCellValue(decisions[i].square.toDouble())
        row.createCell(4).setCellValue(decisions[i].percentage.toDouble())
        row.createCell(5).setCellValue(answersToHuman(decisions[i].answers))
    }
}

private fun answersToHuman(answers: List<AnswerEnum>?): String = answers?.joinToString(",") {
    when (it) {
        AnswerEnum.YES -> "З"
        AnswerEnum.NO -> "П"
        AnswerEnum.UNDEFINED -> "В"
    }
} ?: ""