package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ru.housekeeper.enums.BuildingTypeEnum
import java.time.LocalDateTime

@Entity
@Table(name = "building")
data class Building(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    val active: Boolean = true,

    val name: String,

    val numberOfApartments: Int?,

    val numberOfApartmentsPerFloor: Int,

    @Enumerated(EnumType.STRING)
    val type: BuildingTypeEnum = BuildingTypeEnum.APARTMENT_BUILDING,

    )
