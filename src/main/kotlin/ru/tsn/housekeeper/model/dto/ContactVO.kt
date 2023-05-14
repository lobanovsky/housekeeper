package ru.tsn.housekeeper.model.dto

import ru.tsn.housekeeper.model.entity.Contact
import ru.tsn.enums.RoomTypeEnum

class ContactVO(
    val block: Boolean = false,
    val tenant: Boolean = false,

    val fullName: String? = null,

    val label: String,

    val phone: String? = null,

    val carNumber: String? = null,

    val car: String? = null,

    val email: String? = null,

    val roomType: RoomTypeEnum = RoomTypeEnum.FLAT,

    ) {

    fun toContact() = Contact(
        label = label,
        numbersOfRooms = when (roomType) {
            RoomTypeEnum.FLAT -> "кв.$label"
            RoomTypeEnum.GARAGE -> "мм.$label"
            RoomTypeEnum.OFFICE -> "оф.$label"
        },
        fullName = fullName,
        phone = phone,
        roomType = roomType,
        block = block,
        tenant = tenant,
        carNumber = carNumber,
        car = car
    )
}