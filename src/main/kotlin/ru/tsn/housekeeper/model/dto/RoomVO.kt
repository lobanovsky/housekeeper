package ru.tsn.housekeeper.model.dto

import ru.tsn.housekeeper.model.entity.Room
import ru.tsn.housekeeper.utils.getPercentage
import ru.tsn.enums.RoomTypeEnum
import java.math.BigDecimal

data class RoomVO(

    val street: String? = null,
    val building: String? = null,
    val cadastreNumber: String? = null,

    val account: String? = null,

    val ownerName: String,
    val number: String,
    val certificate: String? = null,
    val square: BigDecimal = BigDecimal.ZERO,
    val percentage: BigDecimal = getPercentage(square),
    val type: RoomTypeEnum = RoomTypeEnum.FLAT,

    val owners: MutableSet<OwnerVO> = mutableSetOf(),
    val tenants: MutableSet<OwnerVO> = mutableSetOf(),
) {
    fun toRoom(checksum: String): Room {
        return Room(
            street = street,
            building = building,
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