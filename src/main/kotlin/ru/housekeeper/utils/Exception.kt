package ru.housekeeper.utils

import ru.housekeeper.exception.EntityNotFoundException


fun entityNotfound(pair: Pair<String, Any>, fieldName: String? = null): Nothing =
    throw EntityNotFoundException("${pair.first} с ${if (fieldName != null) fieldName else "id"}=[${pair.second}] не найден")
