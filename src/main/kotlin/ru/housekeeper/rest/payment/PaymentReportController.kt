package ru.housekeeper.rest.payment

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.excel.toExcelAnnualPayments
import ru.housekeeper.excel.toExcelGroupOfPayments
import ru.housekeeper.excel.toExcelPayments
import ru.housekeeper.model.dto.payment.toIncomingPaymentResponse
import ru.housekeeper.model.dto.payment.toOutgoingPaymentResponse
import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingGropingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingPaymentsFilter
import ru.housekeeper.service.PaymentService
import ru.housekeeper.utils.MAX_SIZE_PER_PAGE_FOR_EXCEL
import ru.housekeeper.utils.getExcelReport
import ru.housekeeper.utils.yyyyMMddDateFormat
import ru.housekeeper.utils.yyyyMMddHHmmssDateFormat
import java.time.LocalDateTime

@CrossOrigin
@RestController
@RequestMapping("/reports")
class PaymentReportController(
    private val paymentService: PaymentService,
) {

    @PostMapping(path = ["/payments/outgoing/grouping"])
    @Operation(summary = "Export outgoing payments by filter to excel")
    fun exportOutgoingGroupingPayments(
        @RequestBody filter: OutgoingGropingPaymentsFilter,
    ): ResponseEntity<ByteArray> {
        val payments = paymentService.findAllOutgoingGroupingPaymentsByCounterparty(filter)
        val fileName = "expense_${filter.startDate?.format(yyyyMMddDateFormat())}-${filter.endDate?.format(yyyyMMddDateFormat())}_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return getExcelReport(fileName) { toExcelGroupOfPayments(payments = payments, filter = filter) }
    }

    @PostMapping(path = ["/payments/outgoing"])
    @Operation(summary = "Export outgoing payments by filter to excel")
    fun exportOutgoingPayments(
        @RequestBody filter: OutgoingPaymentsFilter,
    ): ResponseEntity<ByteArray> {
        val payments = paymentService.findAllOutgoingPaymentsWithFilter(0, MAX_SIZE_PER_PAGE_FOR_EXCEL, filter)
            .toOutgoingPaymentResponse(0, MAX_SIZE_PER_PAGE_FOR_EXCEL)
        val fileName = "outgoing_payments_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return getExcelReport(fileName) { toExcelPayments(payments = payments.content) }
    }

    @PostMapping(path = ["/payments/incoming"])
    @Operation(summary = "Export incoming payments by filter to excel")
    fun exportIncomingPayments(
        @RequestBody filter: IncomingPaymentsFilter,
    ): ResponseEntity<ByteArray> {
        val payments = paymentService.findAllIncomingPaymentsWithFilter(0, MAX_SIZE_PER_PAGE_FOR_EXCEL, filter)
            .toIncomingPaymentResponse(0, MAX_SIZE_PER_PAGE_FOR_EXCEL)
        val fileName = "incoming_payments_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return getExcelReport(fileName) { toExcelPayments(payments = payments.content) }
    }

    @PostMapping(path = ["/counterparties/incoming-payments"])
    @Operation(summary = "Find payments from the company")
    fun findPaymentsFromCompany(
        @RequestBody filter: IncomingPaymentsFilter,
    ): ResponseEntity<ByteArray> {
        val payments = paymentService.findAllIncomingPaymentsWithFilter(0, MAX_SIZE_PER_PAGE_FOR_EXCEL, filter)
            .toIncomingPaymentResponse(0, MAX_SIZE_PER_PAGE_FOR_EXCEL)
        val fileName = "incoming_payments_${filter.fromInn}_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
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