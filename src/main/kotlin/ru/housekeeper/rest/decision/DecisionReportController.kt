package ru.housekeeper.rest.decision

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.excel.toExcelDecisions
import ru.housekeeper.excel.toExcelForTemplate
import ru.housekeeper.service.DecisionService
import ru.housekeeper.utils.yyyyMMddHHmmssDateFormat
import java.time.LocalDateTime

@CrossOrigin
@RestController
@RequestMapping("/reports/decisions")
class DecisionReportController(
    private val decisionService: DecisionService,
) {

    @GetMapping(path = ["/not-voted"])
    @Operation(summary = "Print not voted decisions")
    fun makeNotVotedDecisionsReport(): Int =
        decisionService.printDecision("/not-voted") { decisionService.getNotVoted() }

    @GetMapping(path = ["/selected-to-print"])
    @Operation(summary = "Print selected decisions")
    fun printSelectedDecisionsReport(): Int =
        decisionService.printDecision("/selected-to-print") { decisionService.getPrint() }

    @GetMapping()
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