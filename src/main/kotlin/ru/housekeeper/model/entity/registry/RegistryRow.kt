package ru.housekeeper.model.entity.registry

import ru.housekeeper.enums.registry.RegistryChannelEnum
import java.math.BigDecimal

data class RegistryRow(
    //dd-mm-yyyy
    val date: String,
    //hh-mm-ss
    val time: String,
    val branchNumber: String,
    val cashiersNumber: String,
    val eps: String,
    val account: String,
    val fio: String,
    val address: String,
    //mmYY
    val period: String,
    val amount: BigDecimal,
    val transferAmount: BigDecimal,
    val commission: BigDecimal,
    val channel: RegistryChannelEnum,
) {

    fun toCSVLine() = listOf(
        date,
        time,
        branchNumber,
        cashiersNumber,
        eps,
        account,
        fio,
        address,
        period,
        amount.toString().replace(".", ","),
        transferAmount.toString().replace(".", ","),
        commission.toString().replace(".", ","),
        channel.n
    ).joinToString(";")

}
