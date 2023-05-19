package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import ru.housekeeper.model.dto.AnnualPaymentVO
import ru.housekeeper.model.dto.PaymentVO
import ru.housekeeper.model.filter.CompanyPaymentsFilter
import ru.housekeeper.service.CounterpartyService
import ru.housekeeper.service.PaymentService
import ru.housekeeper.utils.*

@CrossOrigin
@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService,
    private val counterpartyService: CounterpartyService,
) {

    @Operation(summary = "Find all deposits made (outgoing payments)")
    @GetMapping(value = ["/deposits"])
    fun findAllDeposits(): List<DepositResponse> = paymentService.findAllDeposits()
        .map { DepositResponse(it.purpose.getContractNumberFromDepositPurpose(), it.toPaymentVO(outgoingSum = it.sum)) }

    data class DepositResponse(
        val contractNumber: String,
        val payment: PaymentVO,
    )

    @Operation(summary = "Getting taxable and tax-free payments for year (incoming payments)")
    @GetMapping(value = ["/{year}/incoming"])
    fun findAnnualIncomingPayments(
        @PathVariable year: Int,
    ): AnnualPaymentVO = paymentService.findAnnualPayments(year)


    @PostMapping(path = ["/counterparties/{inn}/incoming-payments"])
    @Operation(summary = "Find payments from the company")
    fun findPaymentsFromCompany(
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @PathVariable inn: String,
        @RequestBody filter: CompanyPaymentsFilter,
    ): Page<PaymentVO> = counterpartyService.findAllFromCompanyByFilter(inn, pageNum, pageSize, filter)
        .toIncomingPaymentResponse(pageNum, pageSize)
}