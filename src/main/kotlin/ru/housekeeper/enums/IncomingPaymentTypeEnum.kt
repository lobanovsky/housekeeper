package ru.housekeeper.enums

enum class IncomingPaymentTypeEnum(val description: String) {
    SBER_REGISTRY("Сбер реестр"),
    VTB_REGISTRY("ВТб реестр"),
    DEPOSIT_PERCENTAGES("Проценты по депозиту"),
    DEPOSIT_REFUND("Возврат депозита"),

    TAXABLE("Налогооблагаемый доход"),

    DETERMINATE_ACCOUNT("Платёж от лицевого счёта"),
    NOT_DETERMINATE("Неопределенный платёж"),
    UNKOWN("Пропущенный по правилу")

}