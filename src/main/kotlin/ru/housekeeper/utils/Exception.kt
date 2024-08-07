package ru.housekeeper.utils

import ru.housekeeper.exception.EntityNotFoundException


fun entityNotfound(pair: Pair<String, Any>): Nothing = throw EntityNotFoundException("${pair.first} с id=[${pair.second}] не найден")