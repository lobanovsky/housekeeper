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
@Table(name = "access_info")
data class AccessInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    var active: Boolean = true,

    val blockDateTime: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    val blockReason: AccessBlockReasonEnum? = AccessBlockReasonEnum.MANUAL,

    //куда (where?)
    @Type(JsonType::class)
    @Column(name = "areas", columnDefinition = "jsonb")
    val areas: MutableSet<Long> = mutableSetOf(),

    //кому (who?)
    val ownerId: Long,

    //template: 79266191359
    val phoneNumber: String,

    var phoneLabel: String? = null,

    var tenant: Boolean = false,

    )
