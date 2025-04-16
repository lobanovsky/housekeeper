package ru.housekeeper.model.request

import ru.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import ru.housekeeper.model.entity.gate.LogEntry
import java.time.LocalDateTime
import java.util.*

data class LogEntryRequest(
    val deviceId: String?,
    val deviceKey: String?,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val status: LogEntryStatusEnum = LogEntryStatusEnum.OPENED,
    val method: LogEntryAccessMethodEnum = LogEntryAccessMethodEnum.CLOUD,
    val userName: String?,
    val flatNumber: String?,
    val phoneNumber: String?,
)

fun LogEntryRequest.toLogEntry(
    gateId: Long,
    gateName: String,
    userName: String?,
    flatNumber: String?
): LogEntry {
    return LogEntry(
        gateId = gateId,
        gateName = gateName,
        dateTime = dateTime,
        status = status,
        userName = userName,
        flatNumber = flatNumber,
        method = method,
        phoneNumber = phoneNumber,
        uuid = UUID.randomUUID().toString(),
        deviceId = deviceId,
        deviceKey = deviceKey,
    )
}

