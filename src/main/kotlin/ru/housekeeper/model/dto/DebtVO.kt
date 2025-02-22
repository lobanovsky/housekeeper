package ru.housekeeper.model.dto

import ru.housekeeper.enums.DebtTypeEnum
import ru.housekeeper.enums.RoomTypeEnum
import java.math.BigDecimal

data class DebtVO(
    val room: String,
    val account: String,
    val sum: BigDecimal,
    //номер помещения
    val roomNumber: String,
    //тип помещения
    val roomType: RoomTypeEnum,
    //тип долга (жкх или кап.ремонт)
    val debtType: DebtTypeEnum,

    ) {
}