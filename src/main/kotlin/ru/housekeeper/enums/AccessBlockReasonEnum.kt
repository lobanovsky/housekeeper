package ru.housekeeper.enums

enum class AccessBlockReasonEnum(
    val description: String
) {
    MANUAL("Заблокирован вручную"),
    EXPIRED("Не пользовался шлагбаумом более 3-х месяцев"),
}