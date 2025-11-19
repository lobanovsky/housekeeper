package ru.housekeeper.rest.receipt

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.receipt.ObjectType
import ru.housekeeper.enums.receipt.PaymentType
import ru.housekeeper.service.receipt.ReceiptExtractorService

@CrossOrigin
@RestController
@RequestMapping("/receipt")
class ReceiptController(
    private val extractor: ReceiptExtractorService,
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

}