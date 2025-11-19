package ru.housekeeper.service.receipt

import org.apache.pdfbox.Loader
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class PdfMergeService {

    fun mergePdfPages(vararg pdfs: ByteArray?): ByteArray? {
        val sources = pdfs.filterNotNull()
        if (sources.isEmpty()) return null

        val finalDoc = PDDocument()

        for (pdf in sources) {
            val srcDoc = Loader.loadPDF(pdf)
            PDFMergerUtility().appendDocument(finalDoc, srcDoc)
            srcDoc.close()
        }

        val out = ByteArrayOutputStream()
        finalDoc.save(out)
        finalDoc.close()

        return out.toByteArray()
    }
}
