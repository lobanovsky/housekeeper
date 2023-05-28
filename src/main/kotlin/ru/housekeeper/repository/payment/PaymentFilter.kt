package ru.housekeeper.repository.payment

import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingPaymentsFilter
import ru.housekeeper.repository.equalFilterBy
import ru.housekeeper.repository.filterByDate
import ru.housekeeper.repository.likeFilterBy

fun incomingFilters(filter: IncomingPaymentsFilter): String {
    val predicates = mutableMapOf<String, String>()
    predicates["fromInn"] = likeFilterBy("p.fromInn", filter.fromInn)
    predicates["fromName"] = likeFilterBy("p.fromName", filter.fromName)
    predicates["purpose"] = likeFilterBy("p.purpose", filter.purpose)
    predicates["taxable"] = equalFilterBy("p.taxable", filter.taxable)
    predicates["date"] = filterByDate("cast(p.date as date)", filter.startDate, filter.endDate)
    return predicates.values.joinToString(separator = " ")
}

fun outgoingFilters(filter: OutgoingPaymentsFilter): String {
    val predicates = mutableMapOf<String, String>()
    predicates["toInn"] = likeFilterBy("p.toInn", filter.toInn)
    predicates["toName"] = likeFilterBy("p.toName", filter.toName)
    predicates["purpose"] = likeFilterBy("p.purpose", filter.purpose)
    predicates["taxable"] = equalFilterBy("p.taxable", filter.taxable)
    predicates["date"] = filterByDate("cast(p.date as date)", filter.startDate, filter.endDate)
    return predicates.values.joinToString(separator = " ")
}

