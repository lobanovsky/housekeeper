package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.email.MailingService

@CrossOrigin
@RestController
@RequestMapping("/emails")
class MailingController(
    private val mailingService: MailingService,
) {
    @Operation(summary = "Refusal Of Paper Receipts")
    @PostMapping("/refusal-of-paper-receipts")
    fun sendEmails() = mailingService.refusalOfPaperReceipts()
}