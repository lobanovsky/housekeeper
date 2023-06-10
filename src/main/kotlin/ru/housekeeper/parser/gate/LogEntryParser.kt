package ru.housekeeper.parser.gate

import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import ru.housekeeper.model.dto.gate.LogEntryVO
import ru.housekeeper.model.entity.gate.Gate
import ru.housekeeper.utils.logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class LogEntryParser(private val file: MultipartFile? = null) {

    fun parse(gate: Gate): List<LogEntryVO> {
        logger().info("Start parsing log entries file")
        val content = String(file?.bytes ?: ByteArray(0))
        return parseLogEntries(content).map { toLogEntryVO(it.dateTime, it.message, gate) }
    }

    /**
     * Opened by user:114-1(callR:1):+79269009660
     * Auth. failed user(call):+79101654340
     * W: Num in 320 deleted
     * W: User 3-33 [79807210309] added in 140
     */
    private fun toLogEntryVO(dateTime: LocalDateTime, line: String, gate: Gate): LogEntryVO {
        return when {
            line.startsWith("Opened by user:") -> openParser(dateTime, line, gate)
            line.startsWith("Auth. failed user(") -> authFailedParser(dateTime, line, gate)
            line.startsWith("W: Num in ") -> numDeletedParser(dateTime, line, gate)
            line.startsWith("W: User ") -> userAddedParser(dateTime, line, gate)
            else -> {
                logger().warn("Unknown line: $line")
                LogEntryVO(
                    dateTime = dateTime,
                    status = LogEntryStatusEnum.UNDEFINED,
                    line = line,
                    uuid = makeUUID(dateTime, line, gate),
                )
            }
        }
    }

    /**
     * Opened by user:114-1(call R:1):+79269009660
     * Opened by user:office5-1(call R:1):+74956181424
     * Opened by user:tehnik-kotov(call R:1):+79271058868
     * Opened by user:16-2(APP R:1):+79636110726
     */
    fun openParser(dateTime: LocalDateTime, line: String, gate: Gate): LogEntryVO {
        val user = line.removePrefix("Opened by user:").substringBefore('(')
        val method = line.substringAfter("(").substringBefore(" ")
        val phoneNumber = line.takeLast(11)
        return LogEntryVO(
            dateTime = dateTime,
            status = LogEntryStatusEnum.OPENED,
            method = LogEntryAccessMethodEnum.fromString(method),
            userName = user,
            phoneNumber = phoneNumber,
            line = line,
            uuid = makeUUID(dateTime, phoneNumber, gate),
        )
    }

    /**
     * Auth. failed user(call):+79101654340
     */
    fun authFailedParser(dateTime: LocalDateTime, line: String, gate: Gate): LogEntryVO {
        val method = line.substringAfter("(").substringBefore(")")
        val phoneNumber = line.takeLast(11)
        return LogEntryVO(
            dateTime = dateTime,
            status = LogEntryStatusEnum.AUTH_FAILED,
            method = LogEntryAccessMethodEnum.fromString(method),
            phoneNumber = phoneNumber,
            line = line,
            uuid = makeUUID(dateTime, phoneNumber, gate),
        )
    }

    /**
     * W: Num in 320 deleted
     */
    fun numDeletedParser(dateTime: LocalDateTime, line: String, gate: Gate): LogEntryVO {
        val cell = line.substringAfter("Num in ").substringBefore(" ")
        return LogEntryVO(
            dateTime = dateTime,
            status = LogEntryStatusEnum.NUM_DELETED,
            cell = cell,
            line = line,
            uuid = makeUUID(dateTime, cell, gate),
        )
    }

    /**
     * W: User 3-33 [79807210309] added in 140
     */
    fun userAddedParser(dateTime: LocalDateTime, line: String, gate: Gate): LogEntryVO {
        val user = line.substringAfter("User ").substringBefore(" ")
        val phoneNumber = line.substringAfter("[").substringBefore("]")
        val cell = line.substringAfter("in ")
        return LogEntryVO(
            dateTime = dateTime,
            status = LogEntryStatusEnum.USER_ADDED,
            userName = user,
            phoneNumber = phoneNumber,
            cell = cell,
            line = line,
            uuid = makeUUID(dateTime, phoneNumber, gate),
        )
    }

    private fun makeUUID(dateTime: LocalDateTime, phoneNumber: String, gate: Gate) =
        "${dateTime.year}_${dateTime.monthValue}_${dateTime.dayOfMonth}_${dateTime.hour}_${dateTime.minute}_${dateTime.second}_${phoneNumber}_gate_${gate.id}_${gate.id}"

    fun parseLogEntries(log: String): List<LogEntry> {
        val pattern = "yyyy.MM.dd HH:mm:ss"
        val lines = log.trim().split("\n")
        val entries = mutableListOf<LogEntry>()
        var visitedDateTime: LocalDateTime? = null
        var currentMessage = ""

        for (line in lines) {
            val trimLine = line.trim()
            val matcher = Regex("(\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}:\\d{2}) (.*)").matchEntire(trimLine)

            if (matcher != null) {
                val dateTime = LocalDateTime.parse(matcher.groupValues[1], DateTimeFormatter.ofPattern(pattern))

                if (visitedDateTime != null) {
                    entries.add(LogEntry(visitedDateTime, currentMessage))
                }

                visitedDateTime = dateTime
                currentMessage = matcher.groupValues[2]
            } else {
                currentMessage += " $trimLine"
            }
        }

        if (visitedDateTime != null) {
            entries.add(LogEntry(visitedDateTime, currentMessage))
        }
        //remove duplicates and get unique entries (by dateTime)
        return entries.associateBy { it.dateTime }.values.toList()
    }

    class LogEntry(val dateTime: LocalDateTime, val message: String)
}