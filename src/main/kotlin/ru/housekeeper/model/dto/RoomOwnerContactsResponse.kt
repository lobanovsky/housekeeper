package ru.housekeeper.model.dto

import ru.housekeeper.enums.RoomTypeEnum
import java.math.BigDecimal

data class RoomOwnerContactsResponse(
    val roomId: Long?,
    val buildingId: Long,
    val roomNumber: String,
    val roomType: RoomTypeEnum,
    val roomTypeDescription: String,
    val account: String?,
    val square: BigDecimal,
    val ownerId: Long,
    val ownerFullName: String,
    val phones: List<OwnerContactPhoneResponse>,
)

data class OwnerContactPhoneResponse(
    val phoneNumber: String,
    val fullName: String?,
)
