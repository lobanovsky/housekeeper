package ru.housekeeper.utils

import ru.housekeeper.model.dto.payment.PaymentVO
import ru.housekeeper.model.entity.payment.Payment
import java.math.BigDecimal

enum class FlaggedColorEnum(val color: String, val description: String) {
    RED("#FF0000", "Красный"),
    ORANGE("#FFA500", "Оранжевый"),
    YELLOW("#FFFF00", "Желтый"),
    GREEN("#008000", "Зеленый"),
    BLUE("#0000FF", "Синий"),
    PURPLE("#800080", "Фиолетовый"),
    GRAY("#808080", "Серый"),
    BLACK("#000000", "Черный"),
    WHITE("#FFFFFF", "Белый"),
}


//Перечисление средств во вклад (депозит) по договору 1234567890.ПУ00 от 12.02.2021 . НДС не облагается.
fun String.getContractNumberFromDepositPurpose(): String {
    val split = this.split(".")
    val s = if (split.isNotEmpty()) split[0] else ""
    return s.substring(s.lastIndexOf(" ") + 1)
}

fun List<PaymentVO>.outgoingSum(): BigDecimal =
    this.fold(BigDecimal.ZERO) { acc, p -> acc + (p.outgoingSum ?: BigDecimal.ZERO) }

fun List<PaymentVO>.incomingSum(): BigDecimal =
    this.fold(BigDecimal.ZERO) { acc, p -> acc + (p.incomingSum ?: BigDecimal.ZERO) }

fun List<Payment>.sum(): BigDecimal = this.fold(BigDecimal.ZERO) { acc, p -> acc + (p.sum ?: BigDecimal.ZERO) }