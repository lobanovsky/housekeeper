package ru.housekeeper.service.registry

import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum
import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum.*
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.utils.getOfficeAccount

/**
 * Набор правил
 * Определение платежей не от собственников, а от юридических лиц, от банков и т.д.
 */

//Правила пропуска платежей для обычного счёта для квартир и машиномест
fun rules(payment: IncomingPayment): Boolean {

    if (purposeAndFromContains(
            payment = payment,
            from = "Департамент финансов города",
            purposes = setOf("0313064"),
            type = SUBSIDY
        )
    ) return true

    if (purposeAndFromContains(
            payment = payment,
            from = "Департамент финансов города",
            purposes = setOf("0313068"),
            type = SUBSIDY_FOR_CAPITAL_REPAIR
        )
    ) return true

    if (fromContains(payment, "Департамент финансов города", UNKNOWN)) return true
    if (purposeContains(payment, "Уплачены проценты за период", UNKNOWN)) return true

    //Сбер реестры
    if (purposeContains(payment, "EPS", SBER_REGISTRY)) return true

    //Возврат депозита
    if (purposeContains(payment, "Возврат депозита по договору", DEPOSIT_REFUND)) return true

    //Выплата процентов по депозиту
    if (purposeContains(payment, "Перечислены проценты по договору", DEPOSIT_PERCENTAGES)) return true

    //Выплата процентов по депозиту
    if (purposeContains(payment, "Выплата %%", DEPOSIT_PERCENTAGES)) return true

    //Выплата процентов по депозиту
    if (purposeContains(payment, "Выплата процентов по депозиту", DEPOSIT_PERCENTAGES)) return true

    //ВТБ реестры
    if (purposeAndFromContains(payment, "ВТБ", setOf("_VTB_", "_VTБz"), VTB_REGISTRY)) return true

    //ЕГР, Возврат кредиторской задолженности
    if (purposeContains(payment, "ЕГР", UNKNOWN)) return true

    //Возврат платежа
    if (purposeContains(payment, "Возврат п/п", UNKNOWN)) return true

    //Возврат платежа
    if (purposeContains(payment, "Возврат п/п", UNKNOWN)) return true

    //Исполнительный лист
    if (purposeContains(payment, "ИЛ ФС", UNKNOWN)) return true

    //--- Налогооблагаемые платежи ---

    //ПАО "Ростелеком"
    if (taxableEqInn(payment, "7707049388")) return true
    //Публичное акционерное общество "Московская городская телефонная сеть"
    if (taxableEqInn(payment, "7710016640")) return true
    //АО "ЭР-ТЕЛЕКОМ ХОЛДИНГ"
    if (taxableEqInn(payment, "5902202276")) return true
    //АО "ИСКРАТЕЛЕКОМ"
    if (taxableEqInn(payment, "7736196490")) return true
    //ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ "РОКОСКЛИНИК"
    if (taxableEqInn(payment, "7734401489")
        && !payment.purpose.contains(getOfficeAccount(5))
        && !payment.purpose.contains("Оплата за капитальный ремонт", ignoreCase = true)
        && !payment.purpose.contains("Оплата капитального ремонта", ignoreCase = true)
    ) return true

    return false
}

private fun taxableEqInn(payment: IncomingPayment, inn: String): Boolean {
    if (payment.fromInn.equals(inn)) {
        payment.type = TAXABLE
        return true
    }
    return false
}

private fun purposeContains(
    payment: IncomingPayment,
    other: String,
    type: IncomingPaymentTypeEnum
): Boolean {
    if (payment.purpose.contains(other, ignoreCase = true)) {
        payment.type = type
        return true
    }
    return false
}

private fun fromContains(
    payment: IncomingPayment,
    other: String,
    type: IncomingPaymentTypeEnum
): Boolean {
    if (payment.fromName.contains(other, ignoreCase = true)) {
        payment.type = type
        return true
    }
    return false
}

private fun purposeAndFromContains(
    payment: IncomingPayment,
    from: String,
    purposes: Set<String>,
    type: IncomingPaymentTypeEnum
): Boolean {
    if (payment.fromName.contains(other = from, ignoreCase = true) &&
        purposes.any { purpose -> payment.purpose.contains(purpose, ignoreCase = true) }
    ) {

        payment.type = type
        return true
    }
    return false
}
