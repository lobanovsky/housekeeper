package ru.housekeeper.model.entity.registry

import java.math.BigDecimal

data class RegistrySum(
    var amount: BigDecimal = BigDecimal.ZERO,
    var transferAmount: BigDecimal = BigDecimal.ZERO,
    var commission: BigDecimal = BigDecimal.ZERO,
) {

    fun add(row: RegistryRow) {
        amount = amount.add(row.amount)
        transferAmount = transferAmount.add(row.transferAmount)
        commission = commission.add(row.commission)
    }

}