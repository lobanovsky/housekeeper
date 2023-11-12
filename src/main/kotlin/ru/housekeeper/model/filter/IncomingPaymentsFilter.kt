package ru.housekeeper.model.filter

import ru.housekeeper.enums.IncomingPaymentTypeEnum
import java.time.LocalDate

data class IncomingPaymentsFilter(
    val fromName: String?,
    val fromInn: String?,
    val purpose: String?,
    val taxable: Boolean?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val toAccounts: List<String>?,
    val type: IncomingPaymentTypeEnum?
)