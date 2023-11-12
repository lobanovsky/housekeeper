package ru.housekeeper.model.entity.payment

import jakarta.persistence.*
import ru.housekeeper.enums.IncomingPaymentTypeEnum
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "incoming_payment",
    indexes = [Index(name = "idx_in_uuid", columnList = "uuid"),
        Index(name = "idx_in_fromInn", columnList = "fromInn"),
        Index(name = "idx_in_toInn", columnList = "toInn")]
)
class IncomingPayment(
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
    bik: String = "",
    bankName: String = "",
    purpose: String = "",

    createDate: LocalDateTime = LocalDateTime.now(),

    source: String? = null,

    pack: String = "",

    @Column(nullable = true)
    var account: String? = null,

    @Column(nullable = true)
    var updateAccountDateTime: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var type: IncomingPaymentTypeEnum? = null

) : Payment(id, uuid, date, fromAccount, fromInn, fromName, toAccount, toInn, toName, sum, docNumber, vo, bik, bankName, purpose, createDate, source, pack)