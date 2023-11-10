package ru.housekeeper

import org.junit.jupiter.api.Test
import ru.housekeeper.utils.getFlatAccount
import ru.housekeeper.utils.getOfficeAccount
import ru.housekeeper.utils.getParkingAccount
import ru.housekeeper.utils.getSpecialAccount

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
        //special
        assert(getSpecialAccount(87) == "0000500087")
        assert(getSpecialAccount(123) == "0000500123")
    }
}