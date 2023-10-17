package ru.housekeeper.model.dto

import ru.housekeeper.model.entity.Account

data class AccountResponse(
    val account: String,
    val default: Boolean = true,
    val special: Boolean = false,
    val description: String,
)

fun Account.toAccountResponse() = AccountResponse(
    this.number,
    this.byDefault,
    this.special,
    this.description
)