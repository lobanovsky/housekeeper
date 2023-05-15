package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.excel.*
import ru.housekeeper.service.DecisionService
import ru.housekeeper.utils.*
import java.time.LocalDateTime

@CrossOrigin
@RestController
@RequestMapping("/reports/decisions")
class DecisionReportController(
    private val decisionService: DecisionService,
) {

    @GetMapping(path = ["/decisions/not-voted"])
    @Operation(summary = "Print not voted decisions")
    fun makeNotVotedDecisionsReport(): Int =
        decisionService.printDecision("/not-voted") { decisionService.getNotVoted() }

    @GetMapping(path = ["/decisions/selected-to-print"])
    @Operation(summary = "Print selected decisions")
    fun printSelectedDecisionsReport(): Int =
        decisionService.printDecision("/selected-to-print") { decisionService.getPrint() }

    @GetMapping(path = ["/decisions"])
    @Operation(summary = "Export all decisions")
    fun makeDecisionReport(): ResponseEntity<ByteArray> {
        val decisions = decisionService.findAll()
        val fileName = "Decisions_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(toExcelDecisions(decisions = decisions))
    }

    @GetMapping(path = ["/templates/decisions"])
    @Operation(summary = "Make decisions template")
    fun makeDecisionTemplate(): ResponseEntity<ByteArray> {
        val decisions = decisionService.findAll()
        val fileName = "Template_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}.xlsx"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(toExcelForTemplate(decisions = decisions))
    }

}