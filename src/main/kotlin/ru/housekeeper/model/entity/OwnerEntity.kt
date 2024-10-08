package ru.housekeeper.model.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import ru.housekeeper.enums.SexEnum
import java.time.LocalDateTime


@Entity
@Table(name = "owner")
class OwnerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val fullName: String,

    @Enumerated(EnumType.STRING)
    val sex: SexEnum? = null,

    @Type(JsonType::class)
    @Column(name = "emails", columnDefinition = "jsonb")
    val emails: MutableSet<String> = mutableSetOf(),

    @Type(JsonType::class)
    @Column(name = "phones", columnDefinition = "jsonb")
    val phones: MutableSet<String> = mutableSetOf(),

    val active: Boolean = true,

    val dateOfLeft: LocalDateTime? = null,

    @Type(JsonType::class)
    @Column(name = "rooms", columnDefinition = "jsonb")
    val rooms: MutableSet<Long> = mutableSetOf(),

    var source: String? = null,

    @CreationTimestamp
    @Column(updatable = false)
    val createDate: LocalDateTime = LocalDateTime.now(),

    @Type(JsonType::class)
    @Column(name = "available_access_area", columnDefinition = "jsonb")
    var availableAccessArea: MutableList<Long>? = mutableListOf(),

    )