package ru.housekeeper.model.dto

import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.entity.Room
import ru.housekeeper.utils.getPercentage
import java.math.BigDecimal

data class RoomVO(

    val id: Long? = null,

    val street: String? = null,
    val building: Long,
    val cadastreNumber: String? = null,

    val account: String? = null,

    val ownerName: String,
    val number: String,
    val certificate: String? = null,
    val square: BigDecimal,
    val percentage: BigDecimal = getPercentage(square),
    val type: RoomTypeEnum,
    val typeDescription: String = type.description,

    val owners: MutableSet<OwnerVO>? = null,
    val tenants: MutableSet<OwnerVO>? = null,
) {
    fun toRoom(checksum: String): Room {
        return Room(
            street = street,
            buildingId = building,
            cadastreNumber = cadastreNumber,
            account = account,
            ownerName = ownerName,
            number = number,
            certificate = certificate,
            square = square,
            percentage = percentage,
            type = type,
            source = checksum,
        )
    }
}