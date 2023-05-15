package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "file", indexes = [Index(name = "idx_name", columnList = "name")])
class File(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    val size: Long,

    @Column(nullable = false, unique = true)
    val checksum: String,

    @CreationTimestamp
    @Column(updatable = false)
    val createDate: LocalDateTime? = LocalDateTime.now(),

    )