package ru.housekeeper.model.entity.registry

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "registry")
data class Registry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,


    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    )
