package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.excel.*
import ru.housekeeper.model.filter.CompanyPaymentsFilter
import ru.housekeeper.service.CounterpartyService
import ru.housekeeper.service.PaymentService
import ru.housekeeper.utils.*
import java.time.LocalDateTime

@CrossOrigin
@RestController
@RequestMapping("/reports")
class PaymentReportController(
    private val counterpartyService: CounterpartyService,
    private val paymentService: PaymentService,
) {

    @PostMapping(path = ["/outgoing-payments"])
    @Operation(summary = "Find all outgoing payments by filter")
    fun findAllOutgoingPaymentsByFilter(
        @RequestBody filter: CompanyPaymentsFilter,
    ): ResponseEntity<ByteArray> {
        val payments = paymentService.findAllOutgoingPaymentsByFilter(0, MAX_SIZE_PER_PAGE_FOR_EXCEL, filter)
            .toOutgoingPaymentResponse(0, MAX_SIZE_PER_PAGE_FOR_EXCEL)
        val fileName = "outgoing_payments_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(toExcelPayments(payments = payments.content))
    }

    @PostMapping(path = ["/counterparties/{inn}/incoming-payments"])
    @Operation(summary = "Find payments from the company")
    fun findPaymentsFromCompany(
        @PathVariable inn: String,
        @RequestBody filter: CompanyPaymentsFilter,
    ): ResponseEntity<ByteArray> {
        val payments = counterpartyService.findAllFromCompanyByFilter(inn, 0, MAX_SIZE_PER_PAGE_FOR_EXCEL, filter)
            .toIncomingPaymentResponse(0, MAX_SIZE_PER_PAGE_FOR_EXCEL)
        val fileName = "payments_${inn}_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(toExcelPayments(payments = payments.content))
    }

    @GetMapping(path = ["/incoming-payments/{year}"])
    @Operation(summary = "Getting taxable and tax-free payments for year (incoming payments)")
    fun findAnnualIncomingPayments(
        @PathVariable year: Int,
    ): ResponseEntity<ByteArray> {
        val annualPayments = paymentService.findAnnualPayments(year)
        val fileName = "annual_${year}_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(toExcelAnnualPayments(annualPayment = annualPayments))
    }
}