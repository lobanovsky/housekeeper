package ru.housekeeper.model.filter

import ru.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import java.time.LocalDate

data class LogEntryFilter(
    val gateId: Long?,
    val phoneNumber: String?,
    val userName: String?,
    val flatNumber: String?,
    val status: LogEntryStatusEnum?,
    val method: LogEntryAccessMethodEnum?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    )