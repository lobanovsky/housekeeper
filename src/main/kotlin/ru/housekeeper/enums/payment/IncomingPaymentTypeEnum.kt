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
    //субсидии и компенсации
    SUBSIDY("Субсидия", ColorEnum.GRAY),
    //субсидии за капитальный ремонт
    SUBSIDY_FOR_CAPITAL_REPAIR("Субсидия на капремонт", ColorEnum.GRAY),
    //неопознанные
    UNKNOWN("Неопознанный", ColorEnum.PURPLE)
}