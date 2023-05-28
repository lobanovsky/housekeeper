package ru.housekeeper.repository.gate

import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.gate.LogEntry
import ru.housekeeper.model.filter.LogEntryFilter

interface LogEntryRepositoryCustom {

    fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: LogEntryFilter
    ): Page<LogEntry>
}