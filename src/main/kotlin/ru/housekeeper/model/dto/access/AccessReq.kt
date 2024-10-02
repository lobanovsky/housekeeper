package ru.housekeeper.model.dto.access

import ru.housekeeper.model.entity.access.AccessToArea


data class CreateAccessRequest(
    //куда
    val areas: Set<AccessToArea>,
    //кому
    val person: AccessPerson,
)

data class AccessPerson(
    val ownerId: Long,
    val contacts: Set<Contact>,
)

data class Contact(
    val number: String,
    val label: String? = null,
    val cars: Set<AccessCar>? = mutableSetOf(),
)

data class AccessCar(
    val plateNumber: String,
    val description: String? = null,
)



data class UpdateAccessRequest(
    val label: String? = null,
    val areas: Set<AccessToArea>,
    val cars: Set<AccessCar>? = mutableSetOf(),
)


