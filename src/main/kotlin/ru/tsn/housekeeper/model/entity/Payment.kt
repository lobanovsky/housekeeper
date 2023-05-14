package ru.tsn.housekeeper.model.entity

import jakarta.persistence.*
import ru.tsn.housekeeper.utils.FlaggedColorEnum
import java.math.BigDecimal
import java.time.LocalDateTime

@MappedSuperclass
abstract class Payment(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    var uuid: String = "",

    val date: LocalDateTime = LocalDateTime.now(),

    val fromAccount: String? = null,
    val fromInn: String? = null,
    val fromName: String = "",

    val toAccount: String = "",
    val toInn: String? = null,
    val toName: String = "",

    val sum: BigDecimal? = null,
    val docNumber: String = "",
    val vo: String = "",
    val bik: String = "",
    val bankName: String = "",
    val purpose: String = "",

    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    val source: String? = null,

    val pack: String = "",

    @Enumerated(EnumType.STRING)
    val flagged: FlaggedColorEnum?,

    @Column(name = "taxable")
    val taxable: Boolean? = false,

    @Column(name = "deposit")
    val deposit: Boolean? = false,

)