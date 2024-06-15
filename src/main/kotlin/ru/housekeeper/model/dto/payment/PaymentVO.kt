package ru.housekeeper.model.dto.payment

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.support.PageableExecutionUtils
import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.entity.payment.OutgoingPayment
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentVO(
    val id: Long? = null,
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
    val bankName: String?,

    val purpose: String,

    val account: String? = null,
    val updateAccountDateTime: LocalDateTime? = null,

    val type: IncomingPaymentTypeEnum? = null,
    val typeColor: String? = null,
    val typeName: String? = null,
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
    )
}


fun OutgoingPayment.toPaymentVO(
    incomingSum: BigDecimal? = null,
    outgoingSum: BigDecimal? = null
): PaymentVO = with(this) {
    PaymentVO(
        id = id,
        uuid = uuid,
        date = date,

        fromAccount = fromAccount,
        fromInn = fromInn,
        fromName = fromName,

        toAccount = toAccount,
        toInn = toInn,
        toName = toName,

        incomingSum = incomingSum,
        outgoingSum = outgoingSum,

        docNumber = docNumber,

        vo = vo,

        bik = bik,
        bankName = bankName,

        purpose = purpose,
    )
}


fun IncomingPayment.toPaymentVO(
    incomingSum: BigDecimal? = null,
    outgoingSum: BigDecimal? = null
): PaymentVO = with(this) {
    PaymentVO(
        id = id,
        uuid = uuid,
        date = date,

        fromAccount = fromAccount,
        fromInn = fromInn,
        fromName = fromName,

        toAccount = toAccount,
        toInn = toInn,
        toName = toName,

        incomingSum = incomingSum,
        outgoingSum = outgoingSum,

        docNumber = docNumber,

        vo = vo,

        bik = bik,
        bankName = bankName,

        purpose = purpose,

        account = account,
        updateAccountDateTime = updateAccountDateTime,


        type = type,
        typeColor = type?.color?.color,
        typeName = type?.description

    )
}

fun Page<IncomingPayment>.toIncomingPaymentResponse(pageNum: Int, pageSize: Int): Page<PaymentVO> =
    PageableExecutionUtils.getPage(
        this.content.map { it.toPaymentVO(incomingSum = it.sum) },
        PageRequest.of(pageNum, pageSize)
    ) { this.totalElements }

fun Page<OutgoingPayment>.toOutgoingPaymentResponse(pageNum: Int, pageSize: Int): Page<PaymentVO> =
    PageableExecutionUtils.getPage(
        this.content.map { it.toPaymentVO(incomingSum = it.sum) },
        PageRequest.of(pageNum, pageSize)
    ) { this.totalElements }