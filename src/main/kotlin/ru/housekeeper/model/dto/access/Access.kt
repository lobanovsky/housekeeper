package ru.housekeeper.model.dto.access

data class AccessCreateRequest(
    //куда
    val areas: Set<Long>,
    //кому
    val accessPerson: AccessPerson,
)

data class AccessPerson(
    val ownerId: Long,
    val accessPhones: Set<AccessPhone>,
)

data class AccessPhone(
    val number: String,
    val label: String? = null,
    val tenant: Boolean = false,
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