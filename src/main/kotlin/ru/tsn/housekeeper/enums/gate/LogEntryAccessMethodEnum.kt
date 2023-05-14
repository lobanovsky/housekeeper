package ru.tsn.housekeeper.enums.gate

enum class LogEntryAccessMethodEnum {
    CALL,
    APP,
    UNDEFINED;

    companion object {
        fun fromString(value: String): LogEntryAccessMethodEnum {
            return when (value) {
                "call" -> CALL
                "APP" -> APP
                else -> UNDEFINED
            }
        }
    }
}
