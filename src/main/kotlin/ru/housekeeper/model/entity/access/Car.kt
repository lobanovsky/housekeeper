package ru.housekeeper.model.entity.access

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import java.time.LocalDateTime

@Entity
@Table(name = "car")
data class Car(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    val active: Boolean = true,

    //access to the areas
    @Type(JsonType::class)
    @Column(name = "rooms", columnDefinition = "jsonb")
    val areas: MutableSet<Long?> = mutableSetOf(),

    //car plate number: A123BC999
    //moto plate number: 1234AB99
    val number: String,

    val description: String? = null,

    )
