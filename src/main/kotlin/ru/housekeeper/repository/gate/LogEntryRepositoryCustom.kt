package ru.housekeeper.repository.gate

import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.gate.LogEntry
import ru.housekeeper.model.filter.LogEntryFilter
import ru.housekeeper.rest.gate.LogEntryController
import java.time.LocalDate

interface LogEntryRepositoryCustom {

    fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: LogEntryFilter
    ): Page<LogEntry>

    fun getTop(
        gateId: Long,
        fieldFilter: LogEntryController.FieldFilter,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): List<TopRatingResponse>

    data class TopRatingResponse(
        val count: Long,
        val flatNumber: String? = null,
        val phoneNumber: String? = null,
        val userName: String? = null,
    )

    fun getAllLastNMonths(n: Int): List<LogEntry>
}