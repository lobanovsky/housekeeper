package ru.housekeeper.rest.receipt

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.receipt.ObjectType
import ru.housekeeper.enums.receipt.PaymentType
import ru.housekeeper.model.response.AvailableMonthsResponse
import ru.housekeeper.service.receipt.PdfMergeService
import ru.housekeeper.service.receipt.ReceiptExtractorService
import ru.housekeeper.service.receipt.ReceiptFolderService

@CrossOrigin
@RestController
@RequestMapping("/receipt")
class ReceiptController(
    private val extractor: ReceiptExtractorService,
    private val pdfMerge: PdfMergeService,
    private val folderService: ReceiptFolderService,
) {

    @GetMapping(produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getReceipt(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam type: ObjectType,
        @RequestParam payment: PaymentType,
        @RequestParam number: Int
    ): ResponseEntity<ByteArray> {

        val pdf = extractor.extractReceipt(year, month, type, payment, number)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=receipt-$year-$month-$type-$payment-$number.pdf"
            )
            .body(pdf)
    }


    @GetMapping("/merged", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getMergedReceipt(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam type: ObjectType,
        @RequestParam number: Int
    ): ResponseEntity<*> {

        // 1. Проверяем что есть папка с таким месяцем
        if (!folderService.folderExists(year, month)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "error" to "За выбранный период ($year-$month) квитанции не загружены. Обратитесь в ТСН."
                    )
                )
        }

        val jku = extractor.extractReceipt(year, month, type, PaymentType.JKU, number)
        val kap = extractor.extractReceipt(year, month, type, PaymentType.KAP, number)

        // 2. Если обе квитанции отсутствуют
        val merged = pdfMerge.mergePdfPages(jku, kap) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "Внимание" to "Квитанции за выбранный период отсутствуют. Обратитесь в ТСН."
                )
            )

        // 3. Вернуть PDF
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=receipt-$year-$month-$type-$number.pdf"
            )
            .body(merged)
    }


    // API to list available months
    @GetMapping("/available-months", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAvailableMonths(): AvailableMonthsResponse {
        val list = folderService.getAvailableMonths()
        return AvailableMonthsResponse(months = list)
    }
}