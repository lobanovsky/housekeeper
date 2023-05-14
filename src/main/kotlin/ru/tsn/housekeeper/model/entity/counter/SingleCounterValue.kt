package ru.tsn.housekeeper.model.entity.counter

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ru.tsn.housekeeper.enums.counter.CounterOwnerEnum
import ru.tsn.housekeeper.enums.counter.CounterPositionEnum
import ru.tsn.housekeeper.enums.counter.CounterTypeEnum
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.Month
import java.time.Year

@Entity
@Table(name = "single_counter_value")
class SingleCounterValue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val value: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val dateOfValue: LocalDateTime = LocalDateTime.now(),

    val month: Month = LocalDateTime.now().month,

    val year: Year = Year.now(),

    @Enumerated(EnumType.STRING)
    val counterType: CounterTypeEnum = CounterTypeEnum.COLD_WATER,

    @Enumerated(EnumType.STRING)
    val counterPosition: CounterPositionEnum = CounterPositionEnum.ONE,

    @Enumerated(EnumType.STRING)
    val counterOwner: CounterOwnerEnum = CounterOwnerEnum.INDIVIDUAL,

    //ref to counter
    val counter: Long? = null

)