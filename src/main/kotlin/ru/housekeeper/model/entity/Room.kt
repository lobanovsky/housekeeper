package ru.housekeeper.model.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import ru.housekeeper.enums.RoomTypeEnum
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "room")
class Room(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val street: String? = null,
    val building: String? = null,
    var cadastreNumber: String? = null,

    val account: String? = null,

    val ownerName: String,

    val number: String,
    var certificate: String?,
    val dateOfCertificate: LocalDate? = null,
    val square: BigDecimal = BigDecimal.ZERO,
    val percentage: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    val type: RoomTypeEnum = RoomTypeEnum.FLAT,

    @Type(JsonType::class)
    @Column(name = "owners", columnDefinition = "jsonb")
    val owners: MutableSet<Long?> = mutableSetOf(),

    @Type(JsonType::class)
    @Column(name = "tenants", columnDefinition = "jsonb")
    val tenants: MutableSet<Long?> = mutableSetOf(),

    val source: String? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    )