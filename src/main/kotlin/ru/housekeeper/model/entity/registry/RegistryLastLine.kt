package ru.housekeeper.model.entity.registry

import java.math.BigDecimal

data class RegistryLastLine(
    val numberOfLine: Int,
    val sumAmount: BigDecimal,
    val sumTransferAmount: BigDecimal,
    val sumCommission: BigDecimal,
    val paymentOrderNumber: String,
    //dd-mm-yyyy
    val datePaymentOrder: String,
) {
    fun toCSVLine() = listOf(
        "=$numberOfLine",
        sumAmount.toString().replace(".", ","),
        sumTransferAmount.toString().replace(".", ","),
        sumCommission.toString().replace(".", ","),
        paymentOrderNumber,
        datePaymentOrder
    ).joinToString(";")
}