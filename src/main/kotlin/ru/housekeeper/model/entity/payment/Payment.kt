package ru.housekeeper.model.entity.payment

import jakarta.persistence.*
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
    val bik: String? = null,
    val bankName: String? = null,
    val purpose: String = "",

    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    val source: String? = null,

    @Column(nullable = true)
    val pack: String? = null,

    @Column(columnDefinition = "TEXT")
    val comment: String? = null

    )