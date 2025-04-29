package ru.housekeeper.enums.gate

enum class LogEntryAccessMethodEnum(val description: String) {
    CALL("Звонок"),
    APP("Приложение"),
    CLOUD("Облачный доступ"),
    PROGRESSIVE_WEB_APPS("Прогрессивное веб-приложение"),
    UNDEFINED("Не определено");

    companion object {
        fun fromString(value: String): LogEntryAccessMethodEnum {
            return when (value) {
                "call" -> CALL
                "APP" -> APP
                "CLOUD" -> CLOUD
                "PROGRESSIVE_WEB_APPS" -> PROGRESSIVE_WEB_APPS
                else -> UNDEFINED
            }
        }
    }
}
