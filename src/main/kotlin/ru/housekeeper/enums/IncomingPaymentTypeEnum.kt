package ru.housekeeper.enums

enum class IncomingPaymentTypeEnum(val description: String) {
    DETERMINATE_ACCOUNT("Платёж от лицевого счёта"),
    NOT_DETERMINATE("Неопределенный платёж"),
    SKIP("Пропущенный по правилу")

}