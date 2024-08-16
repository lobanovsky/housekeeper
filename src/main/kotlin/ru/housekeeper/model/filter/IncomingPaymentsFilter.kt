package ru.housekeeper.model.filter

import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum
import java.math.BigDecimal
import java.time.LocalDate

data class IncomingPaymentsFilter(
    val fromName: String? = null,
    val fromInn: String? = null,
    val purpose: String? = null,
    val taxable: Boolean? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val toAccounts: List<String>? = null,
    val type: IncomingPaymentTypeEnum? = null,
    val sum: BigDecimal? = null,
)