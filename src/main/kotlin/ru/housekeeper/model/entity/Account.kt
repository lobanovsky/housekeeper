package ru.housekeeper.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "account")
class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val number: String,

    val byDefault: Boolean = true,

    val special: Boolean = false,

    val description: String,
)