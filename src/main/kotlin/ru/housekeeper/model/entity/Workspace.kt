package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "workspace")
data class Workspace(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @CreationTimestamp
    @Column(updatable = false)
    val createDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    val active: Boolean = true,

    @Column(nullable = false)
    var name: String,

)
