package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.model.dto.AccountResponse

@CrossOrigin
@RestController
@RequestMapping("/accounts")
class AccountController {

    @GetMapping
    @Operation(summary = "Find all accounts")
    fun findAllAccounts(): List<AccountResponse> = listOf(
        AccountResponse("40703810838000014811", false, "До октября 2022"),
        AccountResponse("40703810338000004376", false,  "С ноября 2022"),
        AccountResponse("40705810238000000478", true, "Специальный. Кап. ремонт"),
    )
}

