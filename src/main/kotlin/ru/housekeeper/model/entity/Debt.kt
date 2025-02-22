package ru.housekeeper.model.entity

import jakarta.persistence.*
import ru.housekeeper.enums.DebtTypeEnum
import ru.housekeeper.enums.RoomTypeEnum
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "debt")
class Debt(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val room: String,

    val tag: String,

    val account: String,

    val sum: BigDecimal,

    val fullName: String,

    val square: BigDecimal = BigDecimal.ZERO,

    val roomNumber: String,

    @Enumerated(EnumType.STRING)
    val roomType: RoomTypeEnum,

    @Enumerated(EnumType.STRING)
    val debtType: DebtTypeEnum,

    @Column(updatable = false)
    val createDate: LocalDateTime = LocalDateTime.now(),
)