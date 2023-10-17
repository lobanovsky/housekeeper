package ru.housekeeper.model.dto

data class AccountResponse(
    val account: String,
    val default: Boolean = true,
    val special: Boolean = false,
    val description: String,
)
