package ru.housekeeper.utils

/*
 * 1. ЖКУ (144 + 3 + 144 = 291)
 *    0000001001-0000001144 - квартиры + 0000002003, 0000002005, 0000002008 - коммерческие помещения
 *    0000003001-0000003144 - машиноместа
 */
//0000001068
fun getFlatAccount(number: Int) = "0000001${String.format("%03d", number)}"

//0000003068
fun getParkingAccount(number: Int) = "0000003${String.format("%03d", number)}"

//0000002005
fun getOfficeAccount(number: Int) = "0000002${String.format("%03d", number)}"

/**
 * 2. Капитальный ремонт (144 + 3 + 144 = 291)
 *    0000500001-0000500144 - квартиры + 0000004164, 0000004165, 0000004166 - коммерческие помещения
 *    0000700001-0000700144 - машиноместа
 */
//0000 500087
fun getSpecialAccount(number: Int) = "00005${String.format("%05d", number)}"

//0000 700087
fun getSpecialParkingAccount(number: Int) = "00007${String.format("%05d", number)}"

//0000004164, 0000004165, 0000004166
fun getSpecialOfficeAccount(number: Int) = when (number) {
    3 -> "0000004164"
    5 -> "0000004165"
    8 -> "0000004166"
    else -> throw IllegalArgumentException("Unknown number: $number")
}
