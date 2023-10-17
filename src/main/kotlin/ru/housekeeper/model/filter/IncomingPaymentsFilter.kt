package ru.housekeeper.model.filter

import java.time.LocalDate

data class IncomingPaymentsFilter(
    val fromName: String?,
    val fromInn: String?,
    val purpose: String?,
    val taxable: Boolean?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val toAccounts: List<String>?,
)