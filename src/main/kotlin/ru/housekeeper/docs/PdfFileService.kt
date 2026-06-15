package ru.housekeeper.docs

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File

@Service
class PdfFileService {

    private val breakLine = "Важно отметить, что собственником средств на Спецсчете остаются собственники помещений в МКД. Владелец Спецсчета осуществляет только управление Спецсчетом по поручению собственников."

    // Размеры шрифта зеркалят .docx
    private val baseFontSize = 9f
    private val titleFontSize = 12f
    private val headerFontSize = 10f
    private val blankLineFontSize = 6f

    private val margin = 36f // 0.5 inch

    // TTF кэшируем как байты: PDType0Font привязан к конкретному PDDocument,
    // поэтому сам шрифт создаём на каждый документ, а файл читаем один раз.
    private val regularFontBytes: ByteArray by lazy { loadFontBytes("/fonts/LiberationSerif-Regular.ttf") }
    private val boldFontBytes: ByteArray by lazy { loadFontBytes("/fonts/LiberationSerif-Bold.ttf") }

    fun doIt(rootPath: String, lines: List<String>, path: String, fileName: String, ownerName: String? = null) {
        val ownerNameTrimmed = ownerName?.trim()

        PDDocument().use { document ->
            val regular = PDType0Font.load(document, ByteArrayInputStream(regularFontBytes))
            val bold = PDType0Font.load(document, ByteArrayInputStream(boldFontBytes))

            val pageSize = PDRectangle.A4
            val contentWidth = pageSize.width - 2 * margin

            var page = PDPage(pageSize)
            document.addPage(page)
            var contentStream = PDPageContentStream(document, page)
            var y = pageSize.height - margin

            fun newPage() {
                contentStream.close()
                page = PDPage(pageSize)
                document.addPage(page)
                contentStream = PDPageContentStream(document, page)
                y = pageSize.height - margin
            }

            for ((index, rawLine) in lines.withIndex()) {
                val line = sanitize(stripMarkers(rawLine), regular)
                val header = isHeader(line)
                val ownerLine = ownerNameTrimmed != null && line.trim() == ownerNameTrimmed

                val fontSize = when {
                    line.isBlank() -> blankLineFontSize
                    header && index == 0 -> titleFontSize
                    header -> headerFontSize
                    else -> baseFontSize
                }
                val leading = fontSize * 1.25f

                if (header || ownerLine || line.isBlank()) {
                    // Одношрифтовый путь: заголовки (по центру) / ФИО / пустые строки.
                    val font: PDFont = if (header || ownerLine) bold else regular
                    for (subLine in wrap(line, font, fontSize, contentWidth)) {
                        if (y - leading < margin) newPage()
                        y -= leading
                        if (subLine.isNotEmpty()) {
                            val textWidth = font.getStringWidth(subLine) / 1000f * fontSize
                            val x = if (header) (pageSize.width - textWidth) / 2 else margin
                            contentStream.beginText()
                            contentStream.setFont(font, fontSize)
                            contentStream.newLineAtOffset(x, y)
                            contentStream.showText(subLine)
                            contentStream.endText()
                        }
                    }
                } else {
                    // Обычная строка с возможным инлайн-жирным по маркерам **...**.
                    val words = styledWords(rawLine, regular, bold)
                    val spaceWidth = regular.getStringWidth(" ") / 1000f * fontSize
                    for (wrapped in wrapStyled(words, fontSize, spaceWidth, contentWidth)) {
                        if (y - leading < margin) newPage()
                        y -= leading
                        var x = margin
                        for (word in wrapped) {
                            contentStream.beginText()
                            contentStream.setFont(word.font, fontSize)
                            contentStream.newLineAtOffset(x, y)
                            contentStream.showText(word.text)
                            contentStream.endText()
                            x += word.font.getStringWidth(word.text) / 1000f * fontSize + spaceWidth
                        }
                    }
                }

                if (rawLine == breakLine) newPage()
            }

            contentStream.close()

            val outFile = File("$rootPath${path}/${fileName}")
            outFile.parentFile?.mkdirs()
            document.save(outFile)
        }
    }

    private class StyledWord(val text: String, val font: PDFont)

    private fun stripMarkers(line: String): String = line.replace("**", "")

    // Разбор строки на стилизованные слова: текст между парами ** — жирный.
    // Ведущий отступ строки сохраняется как часть первого слова.
    private fun styledWords(rawLine: String, regular: PDFont, bold: PDFont): List<StyledWord> {
        val indent = rawLine.takeWhile { it == ' ' }
        val segments = rawLine.substring(indent.length).split("**")
        val words = mutableListOf<StyledWord>()
        segments.forEachIndexed { segIndex, segment ->
            if (segment.isEmpty()) return@forEachIndexed
            val font = if (segIndex % 2 == 1) bold else regular
            segment.split(" ").filter { it.isNotEmpty() }.forEach { word ->
                words.add(StyledWord(sanitize(word, font), font))
            }
        }
        if (indent.isNotEmpty() && words.isNotEmpty()) {
            words[0] = StyledWord(indent + words[0].text, words[0].font)
        }
        return words
    }

    // Жадный перенос стилизованных слов по ширине; каждое слово меряется своим шрифтом.
    private fun wrapStyled(
        words: List<StyledWord>,
        fontSize: Float,
        spaceWidth: Float,
        maxWidth: Float,
    ): List<List<StyledWord>> {
        if (words.isEmpty()) return listOf(emptyList())
        val result = mutableListOf<List<StyledWord>>()
        var current = mutableListOf<StyledWord>()
        var currentWidth = 0f
        for (word in words) {
            val wordWidth = word.font.getStringWidth(word.text) / 1000f * fontSize
            val addWidth = if (current.isEmpty()) wordWidth else spaceWidth + wordWidth
            if (current.isEmpty() || currentWidth + addWidth <= maxWidth) {
                current.add(word)
                currentWidth += addWidth
            } else {
                result.add(current)
                current = mutableListOf(word)
                currentWidth = wordWidth
            }
        }
        result.add(current)
        return result
    }

    // Жадный перенос по словам до ширины контента, сохраняя ведущий отступ строки.
    private fun wrap(line: String, font: PDFont, fontSize: Float, maxWidth: Float): List<String> {
        if (line.isBlank()) return listOf("")
        val indent = line.takeWhile { it == ' ' }
        val words = line.substring(indent.length).split(" ").filter { it.isNotEmpty() }
        if (words.isEmpty()) return listOf(line)

        val result = mutableListOf<String>()
        var current = indent
        for (word in words) {
            val tentative = if (current == indent) current + word else "$current $word"
            if (current == indent || width(tentative, font, fontSize) <= maxWidth) {
                current = tentative
            } else {
                result.add(current)
                current = indent + word
            }
        }
        result.add(current)
        return result
    }

    private fun width(text: String, font: PDFont, fontSize: Float): Float =
        font.getStringWidth(text) / 1000f * fontSize

    // Заменяем символы, которых нет в шрифте (и неразрывный пробел), чтобы showText не падал.
    private fun sanitize(text: String, font: PDFont): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            val ch = if (cp == 0x00A0) " " else String(Character.toChars(cp))
            try {
                font.getStringWidth(ch)
                sb.append(ch)
            } catch (e: Exception) {
                sb.append(' ')
            }
            i += Character.charCount(cp)
        }
        return sb.toString()
    }

    // Заголовок: непустая строка без подчёркиваний (исключает строку голосования
    // «ЗА ___ ПРОТИВ ___ ВОЗДЕРЖАЛСЯ ___») и без строчных букв (только верхний регистр).
    private fun isHeader(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.contains('_')) return false
        if (trimmed.none { it.isLetter() }) return false
        return trimmed.none { it.isLetter() && it.isLowerCase() }
    }

    private fun loadFontBytes(resourcePath: String): ByteArray =
        javaClass.getResourceAsStream(resourcePath)?.readBytes()
            ?: error("Font resource not found: $resourcePath")
}
