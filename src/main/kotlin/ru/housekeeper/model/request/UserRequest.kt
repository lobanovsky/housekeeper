package ru.housekeeper.model.request

import ru.housekeeper.enums.UserRoleEnum

data class UserRequest(
    val email: String,
    val name: String,
    val description: String?,
    val role: UserRoleEnum,
)