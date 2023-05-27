package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "template")
class Template(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val header: String,

    @Column(columnDefinition = "TEXT")
    val footer: String,

    @Column(columnDefinition = "TEXT")
    val subject: String,

    @Column(columnDefinition = "TEXT")
    val body: String,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    )