package ru.housekeeper.model.dto.access

data class AccessRequest(
    //куда
    val areas: Set<Long>,
    //кому
    val person: Person,
)

data class Person(
    val ownerId: Long,
    val phones: Set<Phone>,
)

data class Phone(
    val number: String,
    val label: String? = null,
    val tenant: Boolean = false,
)

data class AccessResponse(
    val id: Long? = null,
    val phoneNumber: String,
    val success: Boolean,
    val reason: String? = null,
)