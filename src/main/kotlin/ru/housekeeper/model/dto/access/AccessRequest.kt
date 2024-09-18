package ru.housekeeper.model.dto.access

data class AccessRequest(
    //куда
    val areas: Set<Long>,
    //кому
    val person: Person,
)

data class Person(
    val phones: Set<Phone>,
    val rooms: Set<Room>,
    val tenant: Boolean
)

data class Phone(
    val number: String,
    val label: String? = null,
)

data class Room(
    val buildingId: Long,
    val roomIds: Set<Long>,
)