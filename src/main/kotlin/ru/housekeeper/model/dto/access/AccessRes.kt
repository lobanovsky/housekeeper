package ru.housekeeper.model.dto.access

import ru.housekeeper.model.dto.OwnerVO
import ru.housekeeper.model.entity.access.AccessToArea
import ru.housekeeper.model.entity.access.Car

data class CreateAccessResponse(
    val id: Long? = null,
    val phoneNumber: String,
    val result: CreateAccessResult = CreateAccessResult(true),
)

data class CreateAccessResult(
    val success: Boolean,
    val reason: String? = null,
)


data class AccessVO(
    val id: Long?,
    val owner: OwnerVO,
    val keys: List<KeyVO>?,
)

data class KeyVO(
    val id: Long?,
    val phoneNumber: String,
    val phoneLabel: String? = null,
    val areas: List<AreaVO>,
    val cars: List<CarVO>? = emptyList()
)

data class AreaVO(
    val id: Long?,
    val name: String?,
    val tenant: Boolean?,
    val places: Set<String>? = mutableSetOf(),
)

fun AccessToArea.toAreaVO(name: String?) = AreaVO(
    id = this.areaId,
    name = name,
    tenant = this.tenant,
    places = this.places?.let { if (it.isEmpty()) null else this.places }
)

class CarVO(
    val id: Long?,
    val number: String,
    val description: String? = null,
)

fun Car.toCarVO() = CarVO(this.id, this.plateNumber, this.description)

data class OverviewAccessVO(
    val ownerName: String,
    val ownerRooms: String,

    val phoneNumber: String,
    val phoneLabel: String?,
    val overviewAreas: List<OverviewArea>,

    val carNumber: String,
    val carDescription: String?,
)

data class OverviewArea(
    val areaName: String,
    var tenant: Boolean? = null,
    val places: Set<String>?,
)