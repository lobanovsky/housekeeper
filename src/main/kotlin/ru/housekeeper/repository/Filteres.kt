package ru.housekeeper.repository

import java.time.LocalDate

fun equalFilterBy(parameterName: String, value: String?) =
    if (value?.isNotEmpty() == true) "AND LOWER(${parameterName}) = '${value.lowercase().trim()}'" else ""

fun likeFilterBy(parameterName: String, value: String?) =
    if (value?.isNotEmpty() == true) "AND LOWER(${parameterName}) LIKE '%${value.lowercase().trim()}%'" else ""

fun likeFilterBy(parameterName: String, value: Boolean?) = if (value == true) "AND $parameterName = true" else ""

fun<T: Enum<T>> likeFilterBy(parameterName: String, enumValue: Enum<T>?) = if (enumValue != null) "AND $parameterName = '$enumValue'" else ""

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
