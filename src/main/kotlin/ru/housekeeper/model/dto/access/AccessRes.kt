package ru.housekeeper.model.dto.access

import ru.housekeeper.model.entity.access.AccessEntity
import ru.housekeeper.model.entity.access.Area
import ru.housekeeper.model.entity.access.Car


data class AccessResponse(
    val accessId: Long,

    val active: Boolean,

    val ownerId: Long,

    val areas: Set<AreaResponse>,

    val phoneNumber: String,
    val phoneLabel: String?,
    val tenant: Boolean? = null,

    val cars: List<CarResponse>?,
)

data class AreaResponse(
    val areaId: Long,
    val areaName: String?,
    val places: Set<String>?,
)

data class CarResponse(
    val plateNumber: String,
    val description: String? = null,
)


fun AccessEntity.toAccessResponse(allAreas: Map<Long?, String>) = AccessResponse(
    accessId = id!!,
    active = active,
    ownerId = ownerId,
    areas = areas.map { it.toAreaResponse(allAreas) }.toSet(),
    phoneNumber = phoneNumber,
    phoneLabel = phoneLabel,
    tenant = tenant,
    cars = cars?.map { it.toCarResponse() },
)
fun Area.toAreaResponse(allAreas: Map<Long?, String>) = AreaResponse(areaId, allAreas[areaId], places)
fun Car.toCarResponse() = CarResponse(plateNumber, description)


//Обзор доступов для PWA
data class OverviewResponse(
    val carNumber: String,
    val carDescription: String?,

    val phoneNumber: String,
    val phoneLabel: String?,
    var tenant: Boolean? = null,
    val overviewAreas: List<OverviewArea>,

    val ownerName: String,
    val ownerRooms: String,
    )

data class OverviewArea(
    val areaName: String,
    val places: Set<String>?,
)

fun AccessEntity.toOverviewResponse(allAreas: Map<Long?, String>, ownerName: String, ownerRooms: String) = OverviewResponse(
    carNumber = cars?.firstOrNull()?.plateNumber ?: "",
    carDescription = cars?.firstOrNull()?.description,
    phoneNumber = phoneNumber,
    phoneLabel = phoneLabel,
    tenant = tenant,
    overviewAreas = areas.map { it.toOverviewArea(allAreas) },
    ownerName = ownerName,
    ownerRooms = ownerRooms,
)

fun Area.toOverviewArea(allAreas: Map<Long?, String>) = OverviewArea(allAreas[areaId]!!, places)
