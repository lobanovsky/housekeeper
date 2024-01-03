package ru.housekeeper.repository.account

import ru.housekeeper.model.entity.Account

interface AccountRepositoryCustom {

    fun findBySpecialAndActive(
        special: Boolean,
        active: Boolean,
    ): List<Account>

}