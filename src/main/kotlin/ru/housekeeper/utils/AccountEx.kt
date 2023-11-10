package ru.housekeeper.utils

//0000001068
fun getFlatAccount(number: Int) = "0000001${String.format("%03d", number)}"

//0000002005
fun getOfficeAccount(number: Int) = "0000002${String.format("%03d", number)}"

//0000003068
fun getParkingAccount(number: Int) = "0000003${String.format("%03d", number)}"//0000003068

//0000 500087
fun getSpecialAccount(number: Int) = "00005${String.format("%05d", number)}"