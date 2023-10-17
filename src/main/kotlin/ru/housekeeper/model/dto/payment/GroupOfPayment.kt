package ru.housekeeper.model.dto.payment

import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.model.entity.payment.OutgoingPayment
import java.math.BigDecimal

data class GroupOfPayment(
    val counterparty: Counterparty,
    val payments: MutableList<OutgoingPayment>,
    var total: BigDecimal
) {
    fun addPayment(payment: OutgoingPayment): GroupOfPayment {
        total = total.add(payment.sum)
        payments.add(payment)
        return this
    }
}
