package ru.housekeeper.model.dto.counterparty

import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.utils.simplify

class CounterpartyRequest(
    val originalName: String,
    val inn: String? = null,
    val bank: String,
    val bik: String,
    val sign: String,
    val manualCreated: Boolean = true,
)

fun CounterpartyRequest.toCounterparty() = Counterparty(
    uuid = "${originalName.simplify()} $inn",
    originalName = originalName,
    name = originalName.simplify(),
    inn = inn,
    bank = bank,
    bik = bik,
    sign = sign,
    manualCreated = manualCreated,
)