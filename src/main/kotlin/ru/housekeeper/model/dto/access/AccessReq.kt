package ru.housekeeper.model.dto.access

import ru.housekeeper.model.entity.access.Area
import ru.housekeeper.model.entity.access.Car


data class CreateAccessRequest(
    //собственник, который выдаёт доступы
    val ownerId: Long,
    //доступы
    val accesses: Set<AccessRequest>,
)

data class AccessRequest(
    //куда
    val areas: List<AreaRequest>,
    //кому
    val phoneNumber: String,
    val phoneLabel: String? = null,
    val tenant: Boolean? = null,
    //автомобили
    val cars: Set<CarRequest>? = mutableSetOf(),
)

data class AreaRequest(
    val areaId: Long,
    val places: Set<String>? = null,
)

data class CarRequest(
    val plateNumber: String,
    val description: String? = null,
)

data class UpdateAccessRequest(
    val phoneLabel: String? = null,
    val tenant: Boolean? = null,
    val cars: List<CarRequest>?,
    val areas: List<AreaRequest>,
)

fun CarRequest.toCar() = Car(plateNumber, description)
fun AreaRequest.toArea() = Area(areaId, places)


