package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import ru.housekeeper.model.dto.PaymentResponse
import ru.housekeeper.model.filter.CompanyPaymentsFilter
import ru.housekeeper.service.CounterpartyService
import ru.housekeeper.utils.*

@CrossOrigin
@RestController
@RequestMapping("/counterparties")
class CounterpartyController(
    private val counterpartyService: CounterpartyService,
) {

    @Operation(summary = "Find payments from from company")
    @PostMapping(value = ["/{inn}/payments"])
    fun findAllFromCompanyByFilter(
        @PathVariable inn: String,
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @RequestBody filter: CompanyPaymentsFilter,
    ): PaymentResponse {
        val payments = counterpartyService.findAllFromCompanyByFilter(inn, pageNum, pageSize, filter).toIncomingPaymentResponse(pageNum, pageSize)
        val fromName = if (payments.content.size > 0) payments.content[0].fromName else ""
        return PaymentResponse(
            counterpartyInn = inn,
            counterpartyName = fromName,
            totalSum = payments.content.incomingSum(),
            payments = payments
        )
    }

}