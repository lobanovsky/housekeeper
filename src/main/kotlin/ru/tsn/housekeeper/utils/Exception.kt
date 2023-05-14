package ru.tsn.housekeeper.utils

import ru.tsn.housekeeper.exception.EntityNotFoundException


fun entityNotfound(pair: Pair<String, Any>): Nothing = throw EntityNotFoundException("${pair.first} with [${pair.second}] not found")