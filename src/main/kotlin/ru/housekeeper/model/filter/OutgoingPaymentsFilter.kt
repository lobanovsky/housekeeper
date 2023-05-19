package ru.housekeeper.model.filter

import java.math.BigDecimal
import java.time.LocalDate

data class OutgoingPaymentsFilter(
    val fromName: String?,
    val fromInn: String?,
    val toName: String?,
    val toInn: String?,
    val purpose: String?,
    val sum: BigDecimal?,
    val taxable: Boolean?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)