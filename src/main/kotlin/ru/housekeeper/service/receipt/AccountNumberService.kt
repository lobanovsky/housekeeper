package ru.housekeeper.service.receipt

import org.springframework.stereotype.Service
import ru.housekeeper.enums.receipt.PaymentType
import ru.housekeeper.enums.receipt.RoomType

@Service
class AccountNumberService {

    fun buildAccount(type: RoomType, payment: PaymentType, number: Int): String {
        return when (type to payment) {
            RoomType.FLAT to PaymentType.JKU -> "0000001" + "%03d".format(number)
            RoomType.FLAT to PaymentType.KAP -> "0000500" + "%03d".format(number)

            RoomType.PARKING_SPACE to PaymentType.JKU -> "0000003" + "%03d".format(number)
            RoomType.PARKING_SPACE to PaymentType.KAP -> "0000700" + "%03d".format(number)

            else -> throw IllegalArgumentException("Unsupported combination")
        }
    }

    fun buildSearchString(account: String): String =
        account.chunked(1).joinToString(" ")
}