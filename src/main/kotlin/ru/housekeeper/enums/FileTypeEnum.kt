package ru.housekeeper.enums

enum class FileTypeEnum(val description: String) {
    PAYMENTS("Платежи"),
    COUNTERPARTIES("Контрагенты"),
    ROOMS("Помещения"),
    CONTACTS("Контакты"),
    REGISTRIES("Реестр собственников"),
    ACCOUNTS("Лицевые счета из \"Домовладельца\""),
    ENTRY_LOG("Посещения"),
    DECISIONS("Решения"),
    DECISION_ANSWERS("Ответы решения собственников"),
    COUNTER_WATER_VALUES("Показания счетчиков воды"),

}