package ru.housekeeper.utils

import ru.housekeeper.exception.EntityNotFoundException


fun entityNotfound(pair: Pair<String, Any>): Nothing = throw EntityNotFoundException("${pair.first} with [${pair.second}] not found")