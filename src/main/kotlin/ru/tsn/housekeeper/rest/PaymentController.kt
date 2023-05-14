package ru.tsn.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import ru.tsn.housekeeper.model.dto.AnnualPaymentVO
import ru.tsn.housekeeper.model.dto.PaymentVO
import ru.tsn.housekeeper.service.PaymentService
import ru.tsn.housekeeper.utils.getContractNumberFromDepositPurpose
import ru.tsn.housekeeper.utils.toPaymentVO

@CrossOrigin
@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService,
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

}