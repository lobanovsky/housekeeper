package ru.housekeeper

import ru.housekeeper.utils.isValidBelarusCarNumber
import ru.housekeeper.utils.isValidRussianCarNumber
import kotlin.test.Test

class PlateTest {

    @Test
    fun isValidPlateNumberTest() {
        //АВЕКМНОРСТУХ
        val carNumber1 = "у123вв11"
        val carNumber2 = "х543хх77"
        val carNumber3 = "а564вр31"
        val carNumber4 = "е555кк999"

        assert(isValidRussianCarNumber(carNumber1)) // true
        assert(isValidRussianCarNumber(carNumber2)) // true
        assert(isValidRussianCarNumber(carNumber3)) // true
        assert(isValidRussianCarNumber(carNumber4)) // true
    }


    @Test
    fun isValidBelarusCarNumberTest() {
        //АВЕІКМНОРСТХ
        val carNumber1 = "1234 ав-1"
        val carNumber2 = "АВ 1234-1"
        val carNumber3 = "АВ-1 1234"
        val carNumber4 = "А 1234 А-1"

        assert(isValidBelarusCarNumber(carNumber1)) // true
        assert(isValidBelarusCarNumber(carNumber2)) // true
        assert(isValidBelarusCarNumber(carNumber3)) // true
        assert(isValidBelarusCarNumber(carNumber4)) // true
    }
}