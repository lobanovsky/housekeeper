package ru.tsn.housekeeper.model.dto.counter

import ru.tsn.housekeeper.enums.counter.CounterTypeEnum

data class WaterCounterVO(
    val roomNumber: String,
    val counterNumber: String,
    val counterType: CounterTypeEnum,
)