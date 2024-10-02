package ru.housekeeper.model.entity.access

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import ru.housekeeper.enums.AccessBlockReasonEnum
import java.time.LocalDateTime

/**
 * На парковку около дома - это Фку
 *
 */
@Entity
@Table(name = "access")
data class Access(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    var active: Boolean = true,

    val blockDateTime: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    val blockReason: AccessBlockReasonEnum? = null,

    //куда
    @Type(JsonType::class)
    @Column(name = "areas", columnDefinition = "jsonb")
    val areas: MutableSet<AccessToArea> = mutableSetOf(),

    //Кто выдаёт доступ
    val ownerId: Long,

    //Ключ доступа
    val phoneNumber: String,
    var phoneLabel: String? = null,

    )

data class AccessToArea(
    val areaId: Long,
    //арендатор или владелец
    var tenant: Boolean? = null,
    //конкретные места
    val places: Set<String>? = mutableSetOf(),
    )