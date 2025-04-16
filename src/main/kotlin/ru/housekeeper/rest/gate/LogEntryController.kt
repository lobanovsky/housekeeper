package ru.housekeeper.rest.gate

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import ru.housekeeper.model.dto.gate.LogEntryResponse
import ru.housekeeper.model.filter.LogEntryFilter
import ru.housekeeper.model.request.LogEntryRequest
import ru.housekeeper.service.gate.LogEntryService
import ru.housekeeper.utils.toLogEntryResponse
import java.time.LocalDate

@CrossOrigin
@RestController
@RequestMapping("/log-entries")
class LogEntryController(
    private val logEntryService: LogEntryService
) {

    @GetMapping("/access-methods")
    @Operation(summary = "Get all access-methods of log entry")
    fun getLogEntryAccessMethods(): List<LogEntryAccessMethodResponse> = LogEntryAccessMethodEnum.entries.map {
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
    fun getLogEntryStatuses(): List<LogEntryStatusResponse> = LogEntryStatusEnum.entries.map {
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
    ): Page<LogEntryResponse> =
        logEntryService.findAllWithFilter(pageNum, pageSize, filter).toLogEntryResponse(pageNum, pageSize)


    @Operation(summary = "Get top by flat number")
    @GetMapping("/flat-number/top")
    fun getTopByFlatNumber(
        @RequestParam(value = "gateId", required = true, defaultValue = "1") gateId: Long,
        @RequestParam(value = "startDate", required = false) startDate: LocalDate?,
        @RequestParam(value = "endDate", required = false) endDate: LocalDate?,
    ) = logEntryService.getTop(gateId, FieldFilter.FLAT_NUMBER, startDate, endDate)

    @Operation(summary = "Get top by phone number")
    @GetMapping("/phone-number/top")
    fun getTopByPhoneNumber(
        @RequestParam(value = "gateId", required = true, defaultValue = "1") gateId: Long,
        @RequestParam(value = "startDate", required = false) startDate: LocalDate?,
        @RequestParam(value = "endDate", required = false) endDate: LocalDate?,
    ) = logEntryService.getTop(gateId, FieldFilter.PHONE_NUMBER, startDate, endDate)

    @Operation(summary = "Get log entry overview by phone number")
    @GetMapping("/overview/{phone-number}")
    fun getLastByPhoneNumber(@PathVariable("phone-number") phoneNumber: String) = logEntryService.getOverview(phoneNumber)


    enum class FieldFilter(name: String) {
        FLAT_NUMBER("flat-number"),
        PHONE_NUMBER("phone-number")
    }

    @Operation(summary = "Создать запись в журнале")
    @PostMapping("/entries")
    fun createLogEntry(
        @RequestBody logEntryRequest: LogEntryRequest
    ) = logEntryService.createLogEntry(logEntryRequest)

}
