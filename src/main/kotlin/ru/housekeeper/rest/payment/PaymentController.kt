package ru.housekeeper.rest.payment

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.payment.GroupingPaymentByEnum
import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum
import ru.housekeeper.model.dto.AnnualPaymentVO
import ru.housekeeper.model.dto.payment.PaymentVO
import ru.housekeeper.model.dto.payment.toIncomingPaymentResponse
import ru.housekeeper.model.dto.payment.toOutgoingPaymentResponse
import ru.housekeeper.model.dto.payment.toPaymentVO
import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingGropingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingPaymentsFilter
import ru.housekeeper.service.PaymentService
import ru.housekeeper.utils.getContractNumberFromDepositPurpose

@CrossOrigin
@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {

    @Operation(summary = "Set the manual account for the payment")
    @PatchMapping(value = ["/{id}"])
    fun setManualAccountForPayment(
        @PathVariable id: Long,
        @RequestBody manualAccountRequest: ManualAccountRequest,
    ) = paymentService.setManualAccountForPayment(id, manualAccountRequest.account)

    data class ManualAccountRequest(
        val account: String,
    )

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


    @PostMapping(path = ["/incoming"])
    @Operation(summary = "Find incoming payments with filter")
    fun findIncomingPayments(
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @RequestBody filter: IncomingPaymentsFilter,
    ): Page<PaymentVO> = paymentService.findAllIncomingPaymentsWithFilter(pageNum, pageSize, filter)
        .toIncomingPaymentResponse(pageNum, pageSize)


    @PostMapping(path = ["/outgoing"])
    @Operation(summary = "Find outgoing payments with filter")
    fun findOutgoingPayments(
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @RequestBody filter: OutgoingPaymentsFilter
    ): Page<PaymentVO> = paymentService.findAllOutgoingPaymentsWithFilter(pageNum, pageSize, filter)
        .toOutgoingPaymentResponse(pageNum, pageSize)


    /**
     * Выгрузка исходящих платежей, сгруппированных по контрагенту
     */
    @PostMapping(path = ["/outgoing/grouping"])
    @Operation(summary = "Find outgoing payments with filter and grouping by counterparty")
    fun findOutgoingPaymentsGroupingByCounterparty(
        @RequestBody filter: OutgoingGropingPaymentsFilter
    ) = paymentService.findAllOutgoingGroupingPaymentsByCounterparty(filter)

    @GetMapping(path = ["/types"])
    @Operation(summary = "Find all payment types")
    fun findAllPaymentTypes() =
        IncomingPaymentTypeEnum.values().map { PaymentTypeResponse(it.name, it.description, it.color.color) }

    data class PaymentTypeResponse(
        val type: String,
        val description: String,
        val color: String,
    )

    @GetMapping(path = ["/group-by"])
    @Operation(summary = "Find all payments grouped by {groupBy}")
    fun findAllGroupingPaymentBy() =
        GroupingPaymentByEnum.values().map { GroupPaymentByResponse(it.name, it.description) }

    data class GroupPaymentByResponse(
        val type: String,
        val description: String,
    )

}