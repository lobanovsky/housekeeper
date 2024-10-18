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
        buildingId = getBuildingIdByGateIds(gateId),
        gateId = gateId,
        gateName = gateName,
        status = status.description,
        method = method?.description,
    )
}

private fun getBuildingIdByGateIds(gateId: Long): Long? {
    if (gateId == 1L || gateId == 2L) return 1
    if (gateId == 3L || gateId == 4L) return 2
    return null
}

fun Page<LogEntry>.toLogEntryResponse(pageNum: Int, pageSize: Int): Page<LogEntryResponse> =
    PageableExecutionUtils.getPage(
        this.content.map { it.toLogEntryResponse() },
        PageRequest.of(pageNum, pageSize)
    ) { this.totalElements }
