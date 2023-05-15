package ru.housekeeper.model.dto.counter

import ru.housekeeper.enums.counter.CounterTypeEnum

data class WaterCounterVO(
    val roomNumber: String,
    val counterNumber: String,
    val counterType: CounterTypeEnum,
)