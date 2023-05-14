package ru.tsn.housekeeper.utils

import java.math.BigDecimal

//todo move to settings table
val FLAT_SQUARE: BigDecimal = BigDecimal.valueOf(9054.9)
val GARAGE_SQUARE: BigDecimal = BigDecimal.valueOf(2002.1)
val TOTAL_SQUARE: BigDecimal = FLAT_SQUARE.add(GARAGE_SQUARE)

const val MAX_SIZE_PER_PAGE_FOR_EXCEL = 5000

const val NUMBER_OF_QUESTIONS = 16

const val QUORUM = 67.0