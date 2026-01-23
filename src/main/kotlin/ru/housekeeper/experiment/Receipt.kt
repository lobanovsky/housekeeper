import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

fun extractFlatPage(inputPdf: String, flatNumber: Int, outputDir: String = "."): File? {
    val doc = Loader.loadPDF(File(inputPdf))
    var resultFile: File? = null

    // Формируем корректный лицевой счёт: всегда начинается с "0000001"
    // Пример: кв.1 → 0000001001, кв.83 → 0000001083, кв.143 → 0000001143
    val account = "0000001" + String.format("%03d", flatNumber)
    val searchString = account.chunked(1).joinToString(" ")

    val stripper = PDFTextStripper()
    val totalPages = doc.numberOfPages

    for (i in 0 until totalPages) {
        stripper.startPage = i + 1
        stripper.endPage = i + 1
        val text = stripper.getText(doc)

        if (text.contains("Лицевой счет: $searchString")) {
            val newDoc = PDDocument()
            newDoc.addPage(doc.getPage(i))
            val output = File("$outputDir/кв-$flatNumber.pdf")
            newDoc.save(output)
            newDoc.close()
            resultFile = output
            break
        }
    }

    doc.close()
    return resultFile
}

fun main5() {
    val inputPdf = "/Users/evgeny/Projects/tsn/housekeeper/src/main/kotlin/ru/housekeeper/experiment/2025-10-кв.pdf"
    val flatNumber = 144  // пример
    val result = extractFlatPage(inputPdf, flatNumber)

    if (result != null) {
        println("✅ Файл сохранён: ${result.absolutePath}")
    } else {
        println("❌ Квартира №$flatNumber не найдена.")
    }
}
