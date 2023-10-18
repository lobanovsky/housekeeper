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
    val originalName: String,

    @Column(nullable = false)
    val name: String = "",

    val inn: String? = null,

    val bank: String = "",

    val bik: String = "",

    val sign: String = "",

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    )
