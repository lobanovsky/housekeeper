package ru.housekeeper.model.dto.gate

import java.time.LocalDateTime

data class LogEntryResponse(
    val id: Long? = null,
    val dateTime: LocalDateTime,
    val status: String,
    val userName: String? = null,
    val flatNumber: String? = null,
    val buildingId: Long? = null,
    val method: String? = null,
    val phoneNumber: String? = null,
    val gateId: Long? = null,
    val gateName: String? = null,
)


data class LogEntryOverview(
    val lastLogEntry: LogEntryResponse,
    val lastLogEntries: List<LogEntryResponse>,
    val firstLogEntry: LogEntryResponse?,
    val totalSize: Int,
)