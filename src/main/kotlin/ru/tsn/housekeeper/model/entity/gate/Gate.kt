package ru.tsn.housekeeper.model.entity.gate

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "gate")
class Gate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    @Column(unique = true, nullable = false)
    val imei: String,

    val model: String?,

    val name: String,

    val phoneNumber: String?,

    val firmware: String,
)