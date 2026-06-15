package ru.housekeeper

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.housekeeper.docs.PdfFileService
import java.io.File

class PdfFileServiceTest {

    @Test
    fun `generates pdf with cyrillic content`() {
        val lines = listOf(
            "РЕШЕНИЕ",
            "собственника в МЖД по адресу: г. Москва, 17-й проезд Марьиной Рощи, дом 1.",
            "",
            "ДАННЫЕ СОБСТВЕННИКА:",
            "Лобановский Евгений Владимирович",
            "Машиноместо № 108, 12.20кв.м, доля: 0.11%, кад.№ 77:02:0021009:2540",
            "",
            "РЕШЕНИЯ:",
            "1. Избрать председателем Шурыгина Д.М. (кв. 127) и секретаря общего собрания. " +
                "Длинный текст, который должен перенестись на следующую строку по словам без обрезания по краю страницы.",
            "    ЗА ____________   ПРОТИВ ____________   ВОЗДЕРЖАЛСЯ ____________",
            "Голосование проводится **с 22.06.2026г. до 13.07.2026г.** включительно.",
        )

        val dir = File("etc/test-pdf")
        val fileName = "Тест решение.pdf"
        val outFile = File(dir, fileName)
        outFile.delete()

        PdfFileService().doIt(
            rootPath = "etc",
            lines = lines,
            path = "/test-pdf",
            fileName = fileName,
            ownerName = "Лобановский Евгений Владимирович",
        )

        assertTrue(outFile.exists(), "PDF file was not created")
        assertTrue(outFile.length() > 0, "PDF file is empty")

        val text = Loader.loadPDF(outFile).use { PDFTextStripper().getText(it) }
        assertTrue(text.contains("РЕШЕНИЕ"), "Cyrillic title missing in PDF text: $text")
        assertTrue(text.contains("Лобановский Евгений Владимирович"), "Owner name missing")
        assertTrue(text.contains("Машиноместо"), "Room line missing")
        assertTrue(text.contains("ВОЗДЕРЖАЛСЯ"), "Voting line missing")
        // Маркеры жирного не должны попадать в текст, а сам текст — должен
        assertTrue(text.contains("с 22.06.2026г. до 13.07.2026г."), "Bolded phrase missing")
        assertTrue(!text.contains("**"), "Bold markers leaked into PDF text: $text")

        outFile.delete()
    }
}
