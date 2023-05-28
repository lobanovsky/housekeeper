package ru.housekeeper.enums.gate

enum class LogEntryStatusEnum(val description: String) {
    OPENED("Открыт"),
    AUTH_FAILED("Ошибка авторизации"),
    NUM_DELETED("Номер удален"),
    USER_ADDED("Пользователь добавлен"),
    UNDEFINED("Не определен")
}