package ru.housekeeper.model.dto.payment

data class PaymentCounterparty(
    val account: String,
    val inn: String? = null,
    val name: String,
    val bank: String? = null,
    val bik: String? = null,
)