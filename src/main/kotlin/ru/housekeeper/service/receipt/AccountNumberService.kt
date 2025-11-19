package ru.housekeeper.service.receipt

import org.springframework.stereotype.Service
import ru.housekeeper.enums.receipt.ObjectType
import ru.housekeeper.enums.receipt.PaymentType

@Service
class AccountNumberService {

    fun buildAccount(type: ObjectType, payment: PaymentType, number: Int): String {
        return when (type to payment) {
            ObjectType.KV to PaymentType.JKU -> "0000001" + "%03d".format(number)
            ObjectType.KV to PaymentType.KAP -> "0000500" + "%03d".format(number)

            ObjectType.MM to PaymentType.JKU -> "0000003" + "%03d".format(number)
            ObjectType.MM to PaymentType.KAP -> "0000700" + "%03d".format(number)

            else -> throw IllegalArgumentException("Unsupported combination")
        }
    }

    fun buildSearchString(account: String): String =
        account.chunked(1).joinToString(" ")
}