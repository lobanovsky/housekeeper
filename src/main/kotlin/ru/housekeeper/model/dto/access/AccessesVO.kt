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
)