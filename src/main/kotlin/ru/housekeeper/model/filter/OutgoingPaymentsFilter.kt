package ru.housekeeper.model.filter

import ru.housekeeper.enums.payment.GroupingPaymentByEnum
import java.time.LocalDate

data class OutgoingPaymentsFilter(
    val toName: String? = null,
    val toInn: String? = null,
    val purpose: String? = null,
    val taxable: Boolean? = null,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)

data class OutgoingGropingPaymentsFilter(
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val groupBy: GroupingPaymentByEnum = GroupingPaymentByEnum.CATEGORY,
)