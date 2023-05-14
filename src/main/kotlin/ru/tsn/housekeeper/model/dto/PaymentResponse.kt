package ru.tsn.housekeeper.model.dto

import org.springframework.data.domain.Page
import java.math.BigDecimal


data class PaymentResponse(
    val counterpartyInn: String,
    val counterpartyName: String,
    val totalSum: BigDecimal,
    val payments: Page<PaymentVO>,
)