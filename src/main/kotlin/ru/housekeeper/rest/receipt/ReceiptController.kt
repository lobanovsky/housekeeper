package ru.housekeeper.rest.receipt

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.receipt.PaymentType
import ru.housekeeper.enums.receipt.RoomType
import ru.housekeeper.model.response.AvailableMonthsResponse
import ru.housekeeper.service.receipt.PdfMergeService
import ru.housekeeper.service.receipt.ReceiptExtractorService
import ru.housekeeper.service.receipt.ReceiptFolderService
import ru.housekeeper.utils.logger

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
        @RequestParam type: RoomType,
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
        @RequestParam type: RoomType,
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

        logger().info("Merged PDF generated successfully for year: $year, month: $month, type: ${type.descriptionEN}, number: $number")

        // 3. Вернуть PDF
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=MP17dom1-$year-$month-${type.descriptionEN}-$number.pdf"
            ).header(
                HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                HttpHeaders.CONTENT_DISPOSITION
            )
            .body(merged)
    }


    // API to list available months
    @GetMapping("/available-months", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAvailableMonths(): AvailableMonthsResponse {
        val list = folderService.getAvailableMonths()
        return AvailableMonthsResponse(months = list.sortedByDescending { it })
    }

    //специальная ручка для массвой печати квитанций
    //для этих данных
    //кв: 33, 34, 70, 71, 80, 93, 96, 104, 108, 118, 120, 122, 124, 126, 140, 141
    //мм: 1, 8, 9, 10, 13, 14, 18, 43, 44, 102, 102, 141
    @GetMapping("/batch-print", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getBatchPrintReceipts(
        @RequestParam year: Int,
        @RequestParam month: Int,
    ): ResponseEntity<ByteArray> {
        val apartmentNumbers = listOf(33, 34, 70, 71, 80, 93, 96, 104, 108, 118, 120, 122, 124, 126, 140, 141)
        val parkingNumbers = listOf(1, 8, 9, 10, 13, 14, 18, 43, 44, 102, 102, 141)

        val pdfsToMerge = mutableListOf<ByteArray>()

        for (number in apartmentNumbers) {
            val jkuPdf = extractor.extractReceipt(year, month, RoomType.FLAT, PaymentType.JKU, number)
            val kapPdf = extractor.extractReceipt(year, month, RoomType.FLAT, PaymentType.KAP, number)
            val mergedPdf = pdfMerge.mergePdfPages(jkuPdf, kapPdf)
            if (mergedPdf != null) {
                pdfsToMerge.add(mergedPdf)
            } else {
                logger().warn("Квитанция для квартиры №$number за $year-$month не найдена.")
            }
        }

        for (number in parkingNumbers) {
            val jkuPdf = extractor.extractReceipt(year, month, RoomType.PARKING_SPACE, PaymentType.JKU, number)
            val kapPdf = extractor.extractReceipt(year, month, RoomType.PARKING_SPACE, PaymentType.KAP, number)
            val mergedPdf = pdfMerge.mergePdfPages(jkuPdf, kapPdf)
            if (mergedPdf != null) {
                pdfsToMerge.add(mergedPdf)
            } else {
                logger().warn("Квитанция для машиноместа №$number за $year-$month не найдена.")
            }
        }
        val finalMergedPdf = pdfMerge.mergePdfPages(*pdfsToMerge.toTypedArray())
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(null)

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=MP17dom1-batch-$year-$month.pdf"
            ).header(
                HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                HttpHeaders.CONTENT_DISPOSITION
            )
            .body(finalMergedPdf)
    }
}