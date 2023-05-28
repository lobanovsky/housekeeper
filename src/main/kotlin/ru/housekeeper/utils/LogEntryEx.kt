package ru.housekeeper.utils

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.support.PageableExecutionUtils
import ru.housekeeper.model.dto.gate.LogEntryVO
import ru.housekeeper.model.entity.gate.LogEntry

fun LogEntry.toLogEntryVO(): LogEntryVO = with(this) {
    LogEntryVO(
        id = id,
        dateTime = dateTime,
        status = status,
        userName = userName,
        flatNumber = flatNumber,
        cell = cell,
        method = method,
        phoneNumber = phoneNumber,
        line = line,
        customId = uuid,
    )
}

fun Page<LogEntry>.toLogEntryResponse(pageNum: Int, pageSize: Int): Page<LogEntryVO> =
    PageableExecutionUtils.getPage(
        this.content.map { it.toLogEntryVO() },
        PageRequest.of(pageNum, pageSize)
    ) { this.totalElements }
