package ru.housekeeper.model.dto.counterparty

import ru.housekeeper.model.entity.Counterparty
import java.time.LocalDateTime

data class CounterpartyResponse(
    val id: Long?,
    val name: String,
    val inn: String? = null,
    val bank: String? = null,
    val bik: String? = null,
    val sign: String? = null,
    var createDate: LocalDateTime,
)

fun Counterparty.toResponse() = CounterpartyResponse(
    id = id,
    name = name,
    inn = inn,
    bank = bank,
    bik = bik,
    sign = sign,
    createDate = createDate,
)
