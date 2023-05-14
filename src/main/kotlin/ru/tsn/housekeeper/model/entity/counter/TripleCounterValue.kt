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
@Table(name = "triple_counter_value")
class TripleCounterValue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val t1: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val t2: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val t3: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val dateOfValue: LocalDateTime = LocalDateTime.now(),

    val month: Month = LocalDateTime.now().month,

    val year: Year = Year.now(),

    @Enumerated(EnumType.STRING)
    val counterType: CounterTypeEnum = CounterTypeEnum.ELECTRICITY,

    @Enumerated(EnumType.STRING)
    val counterPosition: CounterPositionEnum = CounterPositionEnum.THREE,

    @Enumerated(EnumType.STRING)
    val counterOwner: CounterOwnerEnum = CounterOwnerEnum.INDIVIDUAL,

    //ref to counter
    val counter: Long? = null

)