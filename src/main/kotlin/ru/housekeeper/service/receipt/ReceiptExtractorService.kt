package ru.housekeeper.service.receipt

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Service
import ru.housekeeper.enums.receipt.ObjectType
import ru.housekeeper.enums.receipt.PaymentType
import java.io.ByteArrayOutputStream
import java.io.File

@Service
class ReceiptExtractorService(
    private val accountService: AccountNumberService
) {

    private val basePath = "/Users/evgeny/Projects/tsn/housekeeper/src/main/resources/receipt"

    fun extractReceipt(
        year: Int,
        month: Int,
        type: ObjectType,
        payment: PaymentType,
        number: Int
    ): ByteArray? {

        val folder = "%04d-%02d".format(year, month)

        val filename = when (payment to type) {
            PaymentType.JKU to ObjectType.KV -> "$folder-кв.pdf"
            PaymentType.JKU to ObjectType.MM -> "$folder-мм.pdf"
            PaymentType.KAP to ObjectType.KV -> "$folder-кап-ремонт-кв.pdf"
            PaymentType.KAP to ObjectType.MM -> "$folder-кап-ремонт-мм.pdf"
            else -> throw IllegalArgumentException("Unsupported")
        }

        val pdfFile = File("$basePath/$folder/$filename")
        if (!pdfFile.exists()) return null

        val account = accountService.buildAccount(type, payment, number)
        val search = "Лицевой счет: " + accountService.buildSearchString(account)

        val doc = Loader.loadPDF(pdfFile)
        val stripper = PDFTextStripper()

        for (i in 0 until doc.numberOfPages) {
            stripper.startPage = i + 1
            stripper.endPage = i + 1

            val text = stripper.getText(doc)
            if (text.contains(search)) {
                val newDoc = PDDocument()
                newDoc.addPage(doc.getPage(i))
                val output = ByteArrayOutputStream()
                newDoc.save(output)
                newDoc.close()
                doc.close()
                return output.toByteArray()
            }
        }

        doc.close()
        return null
    }
}
