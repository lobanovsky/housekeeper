package ru.housekeeper.model.dto.counterparty

import ru.housekeeper.enums.payment.CategoryOfPaymentEnum
import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.service.makeUUID

data class CounterpartyRequest(
    val name: String,
    val inn: String? = null,
)

fun CounterpartyRequest.toCounterparty() = Counterparty(
    uuid = makeUUID(inn, name),
    name = name,
    inn = inn,
    manualCreated = true,
    category = CategoryOfPaymentEnum.UNKNOWN,
    subcategory = CategoryOfPaymentEnum.UNKNOWN,
)