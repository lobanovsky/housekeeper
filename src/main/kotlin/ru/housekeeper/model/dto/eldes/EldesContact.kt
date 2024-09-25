package ru.housekeeper.model.dto.eldes

data class EldesContact(
//    User Name;Tel Number;Relay No.;Sch.1 (1-true 0-false);Sch.2 (1-true 0-false);Sch.3 (1-true 0-false);Sch.4 (1-true 0-false);Sch.5 (1-true 0-false);Sch.6 (1-true 0-false);Sch.7 (1-true 0-false);Sch.8 (1-true 0-false);Year (Valid until);Month (Valid until);Day (Valid until);Hour (Valid until);Minute (Valid until);Ring Counter;Ring Counter Status
    val userName: String,
    val telNumber: String,
    val relayNo: Int = 1,
    val sch1: Int = 0,
    val sch2: Int = 0,
    val sch3: Int = 0,
    val sch4: Int = 0,
    val sch5: Int = 0,
    val sch6: Int = 0,
    val sch7: Int = 0,
    val sch8: Int = 0,
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val hour: String = "",
    val minute: String = "",
    val ringCounter: String = "",
    val ringCounterStatus: String = "",
) {
    fun toCSVLine() = listOf(
        userName,
        telNumber,
        relayNo,
        sch1,
        sch2,
        sch3,
        sch4,
        sch5,
        sch6,
        sch7,
        sch8,
        year,
        month,
        day,
        hour,
        minute,
        ringCounter,
        ringCounterStatus
    ).joinToString(";")
}
