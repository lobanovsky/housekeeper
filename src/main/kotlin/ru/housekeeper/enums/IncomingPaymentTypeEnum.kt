package ru.housekeeper.enums

enum class IncomingPaymentTypeEnum(val description: String, val color: ColorEnum) {
    SBER_REGISTRY("Сбер реестр", ColorEnum.BLUE),
    VTB_REGISTRY("ВТб реестр", ColorEnum.BLUE),
    DEPOSIT_PERCENTAGES("Проценты по депозиту", ColorEnum.YELLOW),
    DEPOSIT_REFUND("Возврат депозита", ColorEnum.YELLOW),

    TAXABLE("Налогооблагаемый доход", ColorEnum.ORANGE),

    DETERMINATE_ACCOUNT("Платёж от лицевого счёта", ColorEnum.GREEN),
    NOT_DETERMINATE("Неопределенный платёж", ColorEnum.RED),
    UNKOWN("Пропущенный по правилу", ColorEnum.RED)

}