package ru.housekeeper.repository

import java.time.LocalDate

fun filterBy(parameterName: String, value: String?) = if (value?.isNotEmpty() == true) "AND LOWER(${parameterName}) LIKE '%${value.lowercase().trim()}%'" else ""
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
