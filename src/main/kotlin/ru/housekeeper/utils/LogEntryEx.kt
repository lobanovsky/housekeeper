package ru.housekeeper.utils

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.support.PageableExecutionUtils
import ru.housekeeper.model.dto.gate.LogEntryResponse
import ru.housekeeper.model.entity.gate.LogEntry

fun LogEntry.toLogEntryResponse(): LogEntryResponse = with(this) {
    LogEntryResponse(
        id = id,
        dateTime = dateTime,
        phoneNumber = phoneNumber,
        userName = userName,
        flatNumber = flatNumber,
        gateId = gateId,
        gateName = gateName,
        status = status.description,
        method = method?.description,
    )
}

fun Page<LogEntry>.toLogEntryResponse(pageNum: Int, pageSize: Int): Page<LogEntryResponse> =
    PageableExecutionUtils.getPage(
        this.content.map { it.toLogEntryResponse() },
        PageRequest.of(pageNum, pageSize)
    ) { this.totalElements }
