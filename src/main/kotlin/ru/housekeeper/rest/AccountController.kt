package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.model.dto.AccountResponse
import ru.housekeeper.model.dto.toAccountResponse
import ru.housekeeper.service.AccountService

@CrossOrigin
@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService,
) {

    @GetMapping
    @Operation(summary = "Find all accounts")
    fun findAllAccounts(): List<AccountResponse> = accountService.getAll().map { it.toAccountResponse()}

}

