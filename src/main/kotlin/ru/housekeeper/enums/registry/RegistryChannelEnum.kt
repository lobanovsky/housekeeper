package ru.housekeeper.enums.registry

enum class RegistryChannelEnum(val n: Int, val description: String) {
    CASH(1, "Касса банка наличные"),
    CARD(2, "Касса банка карта"),
    TERMINAL_CASH(3, "Терминал Сбербанка наличные"),
    TERMINAL_CARD(4, "Терминал Сбербанка карта"),
    ONLINE(5, "Сбербанк Онлайн, Мобильный банк"),
    AUTO_PAY(6, "Автоплатеж");

}