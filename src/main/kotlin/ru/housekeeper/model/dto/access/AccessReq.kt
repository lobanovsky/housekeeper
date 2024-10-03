package ru.housekeeper.model.dto.access

import ru.housekeeper.model.entity.access.AccessToArea
import ru.housekeeper.model.entity.access.Car


data class CreateAccessRequest(
    //куда
    val areas: Set<AccessToArea>,
    //кто выдаёт доступ
    val ownerIds: Set<Long>,
    //кому
    val contacts: Set<Contact>,
)

data class Contact(
    val number: String,
    val label: String? = null,
    val cars: Set<CarRequest>? = mutableSetOf(),
)

data class CarRequest(
    val plateNumber: String,
    val description: String? = null,
)


data class UpdateAccessRequest(
    val label: String? = null,
    val areas: Set<AccessToArea>,
    val cars: Set<CarRequest>? = mutableSetOf(),
)

fun Car.toCarRequest() = CarRequest(plateNumber, description)


