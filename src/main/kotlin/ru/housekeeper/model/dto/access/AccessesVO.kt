package ru.housekeeper.model.dto.access

import ru.housekeeper.model.dto.RoomVO

data class AccessInfoVO(
    val id: Long?,
    val phoneNumber: String,
    val phoneLabel: String? = null,
    val areas: List<AreaVO>,
    val rooms: List<RoomVO>,
)