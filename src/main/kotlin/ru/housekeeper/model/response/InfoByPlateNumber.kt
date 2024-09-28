package ru.housekeeper.model.response

data class InfoByPlateNumber(
    val ownerName: String,
    val ownerRooms: String,

    val phoneNumber: String,
    val phoneLabel: String?,

    val carNumber: String,
    val carDescription: String?,
)
