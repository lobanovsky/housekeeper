package ru.housekeeper.model.filter

import java.time.LocalDate

data class OutgoingPaymentsFilter(
    val toName: String?,
    val toInn: String?,
    val purpose: String?,
    val taxable: Boolean?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)