package ru.housekeeper.model.entity.payment

import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "outgoing_payment",
    indexes = [Index(name = "idx_out_uuid", columnList = "uuid"),
        Index(name = "idx_out_fromInn", columnList = "fromInn"),
        Index(name = "idx_out_toInn", columnList = "toInn")]
)
class OutgoingPayment(
    id: Long? = null,
    uuid: String = "",

    date: LocalDateTime = LocalDateTime.now(),

    fromAccount: String? = null,
    fromInn: String? = null,
    fromName: String = "",

    toAccount: String = "",
    toInn: String? = null,
    toName: String = "",

    sum: BigDecimal? = null,
    docNumber: String = "",
    vo: String = "",
    bik: String? = null,
    bankName: String? = null,
    purpose: String = "",

    createDate: LocalDateTime = LocalDateTime.now(),

    source: String? = null,

    pack: String = "",

    comment: String? = null

) : Payment(id, uuid, date, fromAccount, fromInn, fromName, toAccount, toInn, toName, sum, docNumber, vo, bik, bankName, purpose, createDate, source, pack, comment)