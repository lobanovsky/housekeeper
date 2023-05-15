package ru.housekeeper.model.dto

import java.math.BigDecimal
import java.time.Month


data class MonthPaymentVO(
    val month: Month,
    val numberOfMonth: Int,
    val size: Int,
    val totalSum: BigDecimal,
    val payments: List<PaymentVO>,
)

data class AnnualPaymentVO(
    val year: Int,
    val totalSum: BigDecimal,
    val taxableSum: BigDecimal,
    val taxFreeSum: BigDecimal,
    val depositSum: BigDecimal?,
    val taxablePaymentsByMonths: List<MonthPaymentVO>,
    val taxFreePaymentsByMonths: List<MonthPaymentVO>,
    val depositsPaymentsByMonths: List<MonthPaymentVO>,
)