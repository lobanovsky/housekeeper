package ru.housekeeper.model.dto.counterparty

import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.service.makeUUID

class CounterpartyRequest(
    val name: String,
    val inn: String? = null,
)

fun CounterpartyRequest.toCounterparty() = Counterparty(
    uuid = makeUUID(inn, name),
    name = name,
    inn = inn,
    manualCreated = true,
)