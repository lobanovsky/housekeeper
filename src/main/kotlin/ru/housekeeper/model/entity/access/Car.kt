package ru.housekeeper.model.entity.access

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "car")
data class Car(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    var active: Boolean = true,

    val accessId: Long,

    val plateNumber: String,

    var description: String? = null,
    )
