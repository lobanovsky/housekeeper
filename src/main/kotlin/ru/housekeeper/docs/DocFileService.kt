package ru.housekeeper.docs

import org.apache.poi.xwpf.usermodel.BreakType
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger

@Service
class DocFileService {

    private val breakLine = "Важно отметить, что собственником средств на Спецсчете остаются собственники помещений в МКД. Владелец Спецсчета осуществляет только управление Спецсчетом по поручению собственников."

    // Компактные размеры, чтобы бланк помещался на 2 листа
    private val baseFontSize = 9
    private val titleFontSize = 12
    private val headerFontSize = 10
    private val blankLineFontSize = 6

    fun doIt(rootPath: String, lines: List<String>, path: String, fileName: String, ownerName: String? = null) {

        val document = XWPFDocument()
        narrowMargins(document)
        val ownerNameTrimmed = ownerName?.trim()

        for ((index, line) in lines.withIndex()) {
            val paragraph = document.createParagraph()
            paragraph.spacingBefore = 0
            paragraph.spacingAfter = 0

            val isHeader = isHeader(line)
            val isOwnerName = ownerNameTrimmed != null && line.trim() == ownerNameTrimmed
            paragraph.alignment = if (isHeader) ParagraphAlignment.CENTER else ParagraphAlignment.LEFT

            val run = paragraph.createRun()
            run.fontFamily = "Times New Roman"
            run.isBold = isHeader || isOwnerName
            run.fontSize = when {
                line.isBlank() -> blankLineFontSize
                isHeader && index == 0 -> titleFontSize
                isHeader -> headerFontSize
                else -> baseFontSize
            }
            run.setText(line)

            if (line == breakLine) {
                run.addBreak(BreakType.PAGE)
            }
        }

        val fos = FileOutputStream(File("$rootPath${path}/${fileName}"))
        document.write(fos)
        fos.close()
    }

    // Узкие поля страницы (~1.25 см) — больше места под текст
    private fun narrowMargins(document: XWPFDocument) {
        val sectPr = document.document.body.addNewSectPr()
        val pageMar = sectPr.addNewPgMar()
        val margin = BigInteger.valueOf(720) // 720 twips = 0.5 inch
        pageMar.top = margin
        pageMar.bottom = margin
        pageMar.left = margin
        pageMar.right = margin
    }

    // Заголовок: непустая строка без подчёркиваний (исключает строку голосования
    // «ЗА ___ ПРОТИВ ___ ВОЗДЕРЖАЛСЯ ___») и без строчных букв (только верхний регистр).
    private fun isHeader(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.contains('_')) return false
        if (trimmed.none { it.isLetter() }) return false
        return trimmed.none { it.isLetter() && it.isLowerCase() }
    }
}
