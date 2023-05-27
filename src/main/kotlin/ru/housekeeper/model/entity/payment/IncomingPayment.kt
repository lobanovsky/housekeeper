package ru.housekeeper.model.entity.payment

import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import ru.housekeeper.utils.FlaggedColorEnum
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
    flagged: FlaggedColorEnum?,

    taxable: Boolean? = false,
    deposit: Boolean? = false,

) : Payment(id, uuid, date, fromAccount, fromInn, fromName, toAccount, toInn, toName, sum, docNumber, vo, bik, bankName, purpose, createDate, source, pack, flagged, taxable, deposit)