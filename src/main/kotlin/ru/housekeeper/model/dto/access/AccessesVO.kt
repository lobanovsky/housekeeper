package ru.housekeeper.model.dto.access

import ru.housekeeper.model.dto.OwnerVO

data class AccessInfoVO(
    val owner: OwnerVO,
    val keys: List<KeyVO>,
)

data class KeyVO(
    val id: Long?,
    val phoneNumber: String,
    val phoneLabel: String? = null,
    val tenant: Boolean,
    val areas: List<AreaVO>,
    val cars: List<CarVO>? = emptyList()
)

data class AreaVO(
    val id: Long?,
    val name: String,
    val type: String,
)

class CarVO(
    val id: Long?,
    val number: String,
    val description: String? = null,
)