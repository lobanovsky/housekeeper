package ru.housekeeper.model.entity.counter

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ru.housekeeper.enums.counter.CounterOwnerEnum
import ru.housekeeper.enums.counter.CounterPositionEnum
import ru.housekeeper.enums.counter.CounterTypeEnum
import java.time.LocalDateTime

@Entity
@Table(name = "counter")
class Counter(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val number: String,

    @Enumerated(EnumType.STRING)
    val counterType: CounterTypeEnum = CounterTypeEnum.COLD_WATER,

    @Enumerated(EnumType.STRING)
    val counterPosition: CounterPositionEnum = CounterPositionEnum.ONE,

    @Enumerated(EnumType.STRING)
    val counterOwner: CounterOwnerEnum = CounterOwnerEnum.INDIVIDUAL,

    //ref to room
    val roomId: Long?,

    val account: String?,

    val active: Boolean = true,

    val startDate: LocalDateTime = LocalDateTime.now(),

    val endDate: LocalDateTime = LocalDateTime.now().plusYears(100),

    )