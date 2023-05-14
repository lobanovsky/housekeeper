package ru.tsn.housekeeper.model.dto

data class CounterpartyVO(
    val account: String,
    val inn: String? = null,
    val name: String,
    val bank: String? = null,
    val bik: String? = null,
)
