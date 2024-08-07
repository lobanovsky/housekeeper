package ru.housekeeper.model.entity.access

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import java.time.LocalDateTime

/**
 * На парковку около дома - это Фку
 *
 */
@Entity
@Table(name = "access_info")
data class AccessInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    val active: Boolean = true,

    //куда (where?)
    @Type(JsonType::class)
    @Column(name = "areas", columnDefinition = "jsonb")
    val areas: MutableSet<Long> = mutableSetOf(),

    //кому (who?)
    @Type(JsonType::class)
    @Column(name = "buildings", columnDefinition = "jsonb")
    val buildings: MutableSet<Long> = mutableSetOf(),

    @Type(JsonType::class)
    @Column(name = "rooms", columnDefinition = "jsonb")
    val rooms: MutableSet<Long> = mutableSetOf(),

    @Column(length = 15)
    val label: String,

    //template: 79266191359
    val phoneNumber: String,

    val tenant: Boolean? = false,

    )
