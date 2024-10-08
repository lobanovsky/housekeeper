package ru.housekeeper.model.entity.access

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import ru.housekeeper.enums.AccessBlockReasonEnum
import java.time.LocalDateTime

@Entity
@Table(name = "access")
data class AccessEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    var active: Boolean = true,

    //кто даёт доступ (собственник)
    val ownerId: Long,

    @Type(JsonType::class)
    @Column(name = "areas", columnDefinition = "jsonb")
    val areas: MutableList<Area> = mutableListOf(),

    //Ключ доступа (телефон)
    val phoneNumber: String,
    var phoneLabel: String? = null,
    //арендатор или владелец
    var tenant: Boolean? = null,

    val blockDateTime: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    val blockReason: AccessBlockReasonEnum? = null,

    //cars json
    @Type(JsonType::class)
    @Column(name = "cars", columnDefinition = "jsonb")
    val cars: MutableList<Car>? = null,
)

data class Area(
    val areaId: Long,
    val places: Set<String>? = null,
)

data class Car(
    val plateNumber: String,
    var description: String? = null,
)