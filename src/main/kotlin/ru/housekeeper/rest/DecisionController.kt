package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.DecisionService
import java.math.BigDecimal


@CrossOrigin
@RestController
@RequestMapping("/decisions")
class DecisionController(
    private val decisionService: DecisionService,
) {

    @Operation(summary = "Sent decisions to mail")
    @PostMapping("/send")
    fun sendDecisions(): MailingResponse {
        val (totalDecisions, totalEmails, sentDecisions, sentEmails) = decisionService.sendDecisions(3L) {decisionService.findNotVoted()}
        return MailingResponse(totalDecisions, totalEmails, sentDecisions, sentEmails)
    }

    data class MailingResponse(
        val totalDecisions: Int,
        val totalEmails: Int,
        val sentDecisions: Int,
        val sentEmail: Int,
    )

    @Operation(summary = "Prepare decisions")
    @PostMapping
    fun makeBlankDecision(): DecisionResponse {
        val (totalSize, totalSquare, totalPercentage) = decisionService.prepareDecision()
        return DecisionResponse(totalSize, totalSquare, totalPercentage)
    }

    data class DecisionResponse(
        val totalSize: Int,
        val square: BigDecimal,
        val percentage: BigDecimal
    )
}