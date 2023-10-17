package ru.housekeeper.model.dto

import ru.housekeeper.model.entity.Counterparty

data class CounterpartyInfo(
    val uuid: String,
    val originalName: String,
    val name: String,
    val inn: String,
    val bank: String,
    val bik: String,
    val sign: String,

    )

fun CounterpartyInfo.toCounterparty() = Counterparty(
    uuid = uuid,
    originalName = originalName,
    name = name,
    inn = inn,
    bank = bank,
    bik = bik,
    sign = sign
)
