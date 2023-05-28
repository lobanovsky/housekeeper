package ru.housekeeper.model.dto.gate

import ru.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import ru.housekeeper.model.entity.gate.LogEntry
import java.time.LocalDateTime

class LogEntryVO(
    val id: Long? = null,
    val dateTime: LocalDateTime,
    val status: LogEntryStatusEnum,
    val userName: String? = null,
    val flatNumber: String? = null,
    val cell: String? = null,
    val method: LogEntryAccessMethodEnum? = null,
    val phoneNumber: String? = null,
    val line: String,
    val customId: String,

) {

    fun toLogEntry(source: String, gateId: Long, gateName: String) = LogEntry(
        id = id,
        source = source,
        gateId = gateId,
        gateName = gateName,
        dateTime = dateTime,
        status = status,
        userName = userName,
        flatNumber = getStringBeforeDashIfAllNumbersOrOriginal(userName ?: ""),
        cell = cell,
        method = method,
        phoneNumber = phoneNumber,
        line = line,
        uuid = customId,
    )

    private fun getStringBeforeDashIfAllNumbersOrOriginal(userName: String): String {
        val parts = userName.split("-")
        if (parts.size == 2 && parts[0].matches(Regex("\\d+"))) {
            return parts[0]
        }
        return userName
    }
}
