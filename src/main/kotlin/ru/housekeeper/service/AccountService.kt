package ru.housekeeper.service

import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Account
import ru.housekeeper.repository.account.AccountRepository

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {

    fun getAll(): List<Account> = accountRepository.findAll().toList()
}