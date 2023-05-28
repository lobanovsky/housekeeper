package ru.housekeeper.rest.gate

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import ru.housekeeper.model.dto.gate.LogEntryVO
import ru.housekeeper.model.filter.LogEntryFilter
import ru.housekeeper.service.gate.LogEntryService
import ru.housekeeper.utils.toLogEntryResponse

@CrossOrigin
@RestController
@RequestMapping("/log-entries")
class LogEntryController(
    private val logEntryService: LogEntryService
) {

    @GetMapping("/access-methods")
    @Operation(summary = "Get all access-methods of log entry")
    fun getLogEntryAccessMethods(): List<LogEntryAccessMethodResponse> = LogEntryAccessMethodEnum.values().map {
        LogEntryAccessMethodResponse(
            it.name,
            it.description
        )
    }

    data class LogEntryAccessMethodResponse(
        val name: String,
        val description: String,
    )

    @GetMapping("/statuses")
    @Operation(summary = "Get all statuses of log entry")
    fun getLogEntryStatuses(): List<LogEntryStatusResponse> = LogEntryStatusEnum.values().map {
        LogEntryStatusResponse(
            it.name,
            it.description
        )
    }

    data class LogEntryStatusResponse(
        val name: String,
        val description: String,
    )

    @Operation(summary = "Get all log entries")
    @PostMapping
    fun findAllLogEntries(
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @RequestBody filter: LogEntryFilter,
    ): Page<LogEntryVO> =
        logEntryService.findAllWithFilter(pageNum, pageSize, filter).toLogEntryResponse(pageNum, pageSize)


}
