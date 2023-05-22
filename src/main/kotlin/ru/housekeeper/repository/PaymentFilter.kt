package ru.housekeeper.repository

import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingPaymentsFilter
import java.time.LocalDate

inline fun <reified T> getPage(
    entityManager: EntityManager,
    sql: String,
    sqlCount: String,
    pageNum: Int,
    pageSize: Int
): Page<T> {
    val query = entityManager.createQuery(sql, T::class.java)
    val pageable: Pageable = PageRequest.of(pageNum, pageSize)
    query.firstResult = pageSize * pageNum
    query.maxResults = pageable.pageSize
    return PageableExecutionUtils.getPage(
        query.resultList,
        pageable
    ) { entityManager.createQuery(sqlCount).resultList[0] as Long }
}

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

fun filterBy(parameterName: String, value: String?) =
    if (value?.isNotEmpty() == true) "AND LOWER(${parameterName}) LIKE '%${value.lowercase().trim()}%'" else ""

fun filterBy(parameterName: String, value: Boolean?) = if (value == true) "AND $parameterName = true" else ""

fun filterByDate(parameterName: String, startDate: LocalDate?, endDate: LocalDate?) =
    if (startDate != null || endDate != null) {
        if (startDate != null && endDate != null) {
            "AND ($parameterName BETWEEN '${startDate}' AND '${endDate}')"
        } else if (startDate != null) {
            "AND ($parameterName >= '${startDate}')"
        } else {
            "AND ($parameterName <= '${endDate}')"
        }
    } else ""
