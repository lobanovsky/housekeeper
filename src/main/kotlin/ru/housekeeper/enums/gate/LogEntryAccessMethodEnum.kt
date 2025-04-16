package ru.housekeeper.enums.gate

enum class LogEntryAccessMethodEnum(val description: String) {
    CALL("Звонок"),
    APP("Приложение"),
    CLOUD("Облачный доступ"),
    UNDEFINED("Не определено");

    companion object {
        fun fromString(value: String): LogEntryAccessMethodEnum {
            return when (value) {
                "call" -> CALL
                "APP" -> APP
                "CLOUD" -> CLOUD
                else -> UNDEFINED
            }
        }
    }
}
