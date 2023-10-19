package ru.housekeeper.model.dto.counterparty

import ru.housekeeper.model.entity.Counterparty

data class CounterpartyVO(
    var uuid: String,
    val name: String,
    val inn: String? = null,
    val bank: String? = null,
    val bik: String? = null,
    val sign: String? = null,
)

fun CounterpartyVO.toCounterparty() = Counterparty(
    uuid = uuid,
    name = name,
    inn = inn,
    bank = bank,
    bik = bik,
    sign = sign,
)
