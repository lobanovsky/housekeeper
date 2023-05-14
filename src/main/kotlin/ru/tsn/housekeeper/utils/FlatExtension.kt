package ru.tsn.housekeeper.utils

import java.math.BigDecimal
import java.math.RoundingMode

fun getPercentage(square: BigDecimal, totalSquare: BigDecimal = TOTAL_SQUARE): BigDecimal = square.multiply(BigDecimal.valueOf(100)).divide(totalSquare, 2, RoundingMode.HALF_EVEN)
