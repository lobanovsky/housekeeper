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

    /**
     * UUID контрагента
     * ИНН или упрощенное название, если ИНН не указан
     */
    @Column(unique = true, nullable = false)
    var uuid: String,

    @Column(nullable = false)
    var name: String,

    var inn: String? = null,

    var bank: String? = null,

    var bik: String? = null,

    val sign: String? = null,

    @Column(nullable = true)
    val manualCreated: Boolean? = false,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    )
