package ru.housekeeper.repository.payment

import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingPaymentsFilter
import ru.housekeeper.repository.filterBy
import ru.housekeeper.repository.filterByDate

fun incomingFilters(filter: IncomingPaymentsFilter): String {
    val predicates = mutableMapOf<String, String>()
    predicates["fromInn"] = filterBy("p.fromInn", filter.fromInn)
    predicates["fromName"] = filterBy("p.fromName", filter.fromName)
    predicates["purpose"] = filterBy("p.purpose", filter.purpose)
    predicates["taxable"] = filterBy("p.taxable", filter.taxable)
    predicates["date"] = filterByDate("cast(p.date as date)", filter.startDate, filter.endDate)
    return predicates.values.joinToString(separator = " ")
}

fun outgoingFilters(filter: OutgoingPaymentsFilter): String {
    val predicates = mutableMapOf<String, String>()
    predicates["toInn"] = filterBy("p.toInn", filter.toInn)
    predicates["toName"] = filterBy("p.toName", filter.toName)
    predicates["purpose"] = filterBy("p.purpose", filter.purpose)
    predicates["taxable"] = filterBy("p.taxable", filter.taxable)
    predicates["date"] = filterByDate("cast(p.date as date)", filter.startDate, filter.endDate)
    return predicates.values.joinToString(separator = " ")
}

