package ru.housekeeper.enums.payment

import ru.housekeeper.enums.ColorEnum

enum class IncomingPaymentTypeEnum(val description: String, val color: ColorEnum) {
    //реестры
    SBER_REGISTRY("Сбер реестр", ColorEnum.BLUE),
    VTB_REGISTRY("ВТб реестр", ColorEnum.BLUE),
    //депозиты
    DEPOSIT_PERCENTAGES("Проценты по депозиту", ColorEnum.YELLOW),
    DEPOSIT_REFUND("Возврат депозита", ColorEnum.YELLOW),
    //налоги
    TAXABLE("Налогооблагаемый доход", ColorEnum.ORANGE),
    //лицевые счета
    ACCOUNT("Л/с", ColorEnum.GREEN),
    UNKNOWN_ACCOUNT("Неопределенный л/с", ColorEnum.RED),
    //неопознанные
    UNKNOWN("Неопознанный", ColorEnum.PURPLE)
}