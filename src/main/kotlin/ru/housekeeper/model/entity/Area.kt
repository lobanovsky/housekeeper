package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ru.housekeeper.enums.AreaTypeEnum
import java.time.LocalDateTime

@Entity
@Table(name = "area")
data class Area(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    val active: Boolean = true,

    val name: String,

    val description: String? = null,

    @Enumerated(EnumType.STRING)
    val type: AreaTypeEnum = AreaTypeEnum.YARD_AREA,

    )
