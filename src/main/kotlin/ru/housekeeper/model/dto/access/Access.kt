package ru.housekeeper.model.dto.access

data class AccessCreateRequest(
    //куда
    val areas: Set<Long>,
    //кому
    val person: AccessPerson,
)

data class AccessPerson(
    val ownerId: Long,
    val phones: Set<AccessPhone>,
)

data class AccessPhone(
    val number: String,
    val label: String? = null,
    val tenant: Boolean = false,
    val cars: Set<AccessCar>? = mutableSetOf(),
)

data class AccessCar(
    val plateNumber: String,
    val description: String? = null,
)

data class AccessCreateResponse(
    val id: Long? = null,
    val phoneNumber: String,
    val success: Boolean,
    val reason: String? = null,
)

data class AccessUpdateRequest(
    val label: String? = null,
    val tenant: Boolean = false,
    val areas: Set<Long>,
)