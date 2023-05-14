package ru.tsn.housekeeper.model.dto

import ru.tsn.housekeeper.model.entity.IncomingPayment
import ru.tsn.housekeeper.model.entity.OutgoingPayment
import ru.tsn.housekeeper.utils.FlaggedColorEnum
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentVO(
    val uuid: String,

    val date: LocalDateTime,

    val fromAccount: String?,
    val fromInn: String?,
    val fromName: String,

    val toAccount: String,
    val toInn: String?,
    val toName: String,

    val outgoingSum: BigDecimal?,
    val incomingSum: BigDecimal?,

    val docNumber: String,

    val vo: String,

    val bik: String,
    val bankName: String,

    val purpose: String,
    val tag: FlaggedColorEnum? = null,

    val taxable: Boolean? = null,
    val deposit: Boolean? = null,
) {
    fun toIncomingPayment(
        createDate: LocalDateTime = LocalDateTime.now(),
        fileName: String,
        pack: String
    ) = IncomingPayment(
        uuid = uuid,
        date = date,
        fromAccount = fromAccount,
        fromInn = fromInn,
        fromName = fromName,
        toAccount = toAccount,
        toInn = toInn,
        toName = toName,
        sum = incomingSum,
        docNumber = docNumber,
        vo = vo,
        bik = bik,
        bankName = bankName,
        purpose = purpose,
        createDate = createDate,
        source = fileName,
        pack = pack,
        flagged = tag
    )

    fun toOutgoingPayment(
        createDate: LocalDateTime = LocalDateTime.now(),
        fileName: String,
        pack: String
    ) = OutgoingPayment(
        uuid = uuid,
        date = date,
        fromAccount = fromAccount,
        fromInn = fromInn,
        fromName = fromName,
        toAccount = toAccount,
        toInn = toInn,
        toName = toName,
        sum = outgoingSum,
        docNumber = docNumber,
        vo = vo,
        bik = bik,
        bankName = bankName,
        purpose = purpose,
        createDate = createDate,
        source = fileName,
        pack = pack,
        flagged = tag
    )
}