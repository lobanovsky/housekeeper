package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime


@Entity
@Table(name = "counterparty")
class Counterparty(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    var uuid: String = "",

    @Column(nullable = false)
    var originalName: String,

    @Column(nullable = false)
    var name: String = "",

    var inn: String? = null,

    var bank: String = "",

    var bik: String = "",

    val sign: String = "",

    @Column(nullable = true)
    val manualCreated: Boolean? = false,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    )
