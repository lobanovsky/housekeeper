package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "area")
data class AreaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    val active: Boolean = true,

    val name: String,

    //specific place with diapason of area
    val specificPlace: Boolean? = false,
    val fromNumber: Int? = null,
    val toNumber: Int? = null,

    )
