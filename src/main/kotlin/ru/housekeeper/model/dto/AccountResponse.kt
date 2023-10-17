package ru.housekeeper.model.dto

data class AccountResponse(
    val account: String,
    val special: Boolean = false,
    val description: String,
)
