package ru.housekeeper.service.registry

import ru.housekeeper.enums.IncomingPaymentTypeEnum
import ru.housekeeper.enums.IncomingPaymentTypeEnum.*
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.utils.getOfficeAccount


//Пропуск платежей по правилам
fun nonAccountRules(payment: IncomingPayment): Boolean {

    //Сбер реестры
    if (purposeContains(payment, "EPS", SBER_REGISTRY)) return true

    //Возврат депозита
    if (purposeContains(payment, "Возврат депозита по договору", DEPOSIT_REFUND)) return true

    //Выплата процентов по депозиту
    if (purposeContains(payment, "Перечислены проценты по договору", DEPOSIT_PERCENTAGES)) return true

    //Выплата процентов по депозиту
    if (purposeContains(payment, "Выплата %%", DEPOSIT_PERCENTAGES)) return true

    //ВТБ реестры
    if (purposeContains(payment, "_VTB_", VTB_REGISTRY)) return true

    //ЕГР, Возврат кредиторской задолженности
    if (purposeContains(payment, "ЕГР", UNKOWN)) return true

    //Налогооблагаемые платежи

    //ПАО "Ростелеком"
    if (taxableEqInn(payment, "7707049388")) return true
    //Публичное акционерное общество "Московская городская телефонная сеть"
    if (taxableEqInn(payment, "7710016640")) return true
    //АО "ЭР-ТЕЛЕКОМ ХОЛДИНГ"
    if (taxableEqInn(payment, "5902202276")) return true
    //АО "ИСКРАТЕЛЕКОМ"
    if (taxableEqInn(payment, "7736196490")) return true
    //ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ "РОКОСКЛИНИК"
    if (taxableEqInn(payment, "7734401489") && !payment.purpose.contains(getOfficeAccount(5))) return true

    return false
}

private fun taxableEqInn(payment: IncomingPayment, inn: String): Boolean {
    if (payment.fromInn.equals(inn)) {
        payment.type = TAXABLE
        return true
    }
    return false
}

private fun purposeContains(payment: IncomingPayment, other: String, type: IncomingPaymentTypeEnum): Boolean {
    if (payment.purpose.contains(other, ignoreCase = true)) {
        payment.type = type
        return true
    }
    return false
}

