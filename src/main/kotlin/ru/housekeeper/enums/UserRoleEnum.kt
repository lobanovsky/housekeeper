package ru.housekeeper.enums

enum class UserRoleEnum(
    val roleName: String,
    val description: String,
) {

    SUPER_ADMIN("Супер-Админ", "Полный доступ во все рабочие пространства"),
    ADMIN("Админ", "Администратор рабочего пространства"),
    STAFF_OPERATOR("Сотрудник", "Диспетчер или оператор организации"),
    STAFF_ACCOUNTANT("Бухгалтер", "Бухгалтер организации"),
    STAFF_READ_ONLY("Сотрудник", "Сотрудник с ограниченными правами (только просмотр)"),
    OWNER("Пользователь", "Собственник помещения"),
}