package ru.housekeeper.model.dto.counterparty

import ru.housekeeper.model.entity.Counterparty
import java.time.LocalDateTime

data class CounterpartyResponse(
    val id: Long?,
    val originalName: String,
    val inn: String? = null,
    val bank: String,
    val bik: String,
    val sign: String,
    var createDate: LocalDateTime,
)

fun Counterparty.toResponse() = CounterpartyResponse(
    id = id,
    originalName = originalName,
    inn = inn,
    bank = bank,
    bik = bik,
    sign = sign,
    createDate = createDate,
)
