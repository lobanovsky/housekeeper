package ru.housekeeper.model.filter

import ru.housekeeper.enums.RoomTypeEnum

data class RoomFilter(
    val account: String? = null,
    val type: RoomTypeEnum? = null,
    val number: String? = null,
    val building: String? = null,
    val street: String? = null,
    val ownerName: String? = null,
)
