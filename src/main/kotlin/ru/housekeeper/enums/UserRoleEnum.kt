package ru.housekeeper.enums

enum class UserRoleEnum(
    val roleName: String,
    val description: String,
) {

    SUPER_ADMIN("Супер-Админ", "Полный доступ во все рабочие пространства"),
    ADMIN("Админ", "Администратор рабочего пространства"),
    USER("Пользователь", "Пользователь рабочего пространства"),
}