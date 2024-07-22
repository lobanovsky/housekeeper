package ru.housekeeper

import org.junit.jupiter.api.Test
import ru.housekeeper.utils.*

class AccountFormatTest {

    @Test
    fun check() {
        //flat
        assert(getFlatAccount(1) == "0000001001")
        assert(getFlatAccount(11) == "0000001011")
        assert(getFlatAccount(144) == "0000001144")
        //parking
        assert(getParkingAccount(144) == "0000003144")
        assert(getParkingAccount(14) == "0000003014")
        //office
        assert(getOfficeAccount(5) == "0000002005")

        //special flat
        assert(getSpecialAccount(87) == "0000500087")
        assert(getSpecialAccount(123) == "0000500123")
        //special parking
        assert(getSpecialParkingAccount(7) == "0000700007")
        assert(getSpecialParkingAccount(117) == "0000700117")
        //special office
        assert(getSpecialOfficeAccount(3) == "0000004164")
        assert(getSpecialOfficeAccount(5) == "0000004165")
        assert(getSpecialOfficeAccount(8) == "0000004166")

    }
}