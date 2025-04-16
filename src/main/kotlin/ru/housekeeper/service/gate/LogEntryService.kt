package ru.housekeeper.service.gate

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.gate.LogEntryOverview
import ru.housekeeper.model.entity.gate.LogEntry
import ru.housekeeper.model.filter.LogEntryFilter
import ru.housekeeper.model.request.LogEntryRequest
import ru.housekeeper.model.request.toLogEntry
import ru.housekeeper.parser.gate.LogEntryParser
import ru.housekeeper.repository.gate.LogEntryRepository
import ru.housekeeper.rest.gate.LogEntryController
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.toLogEntryResponse
import java.time.LocalDate

@Service
class LogEntryService(
    private val gateService: GateService,
    private val logEntryRepository: LogEntryRepository,
) {

    @Transactional
    fun createLogEntry(logEntryRequest: LogEntryRequest): LogEntry {
        val gate = gateService.getAllGates()
            .firstOrNull { it.deviceId == logEntryRequest.deviceId }
        if (gate == null) let { throw IllegalArgumentException("Ограждающее устройство с deviceId = ${logEntryRequest.deviceId}/${logEntryRequest.gateName} не найдено в базе данных") }
        return logEntryRepository.save(
            logEntryRequest.toLogEntry(
                gateId = gate.id,
                userName = logEntryRequest.userName,
                flatNumber = logEntryRequest.flatNumber,
            ).let { logEntry ->
                logger().info("Try to save log entry: ${logEntryRequest.deviceId}/${logEntryRequest.gateName}")
                logEntryRepository.save(logEntry)
            })
    }

    data class UploadLogEntriesInfo(
        val totalSize: Int,
    )

    @Synchronized
    fun parseAndSave(file: MultipartFile, checkSum: String, imei: String): UploadLogEntriesInfo {
        val gate = gateService.getGateByImei(imei)
        if (gate == null) {
            logger().error("Gate with IMEI $imei not found")
            throw IllegalArgumentException("Gate with IMEI $imei not found")
        }
        logger().info("Found gate: ${gate.name}")

        val logEntries = LogEntryParser(file).parse(gate)
            .map { it.toLogEntry(checkSum, gateId = gate.id, gateName = gate.name) }
        logger().info("Parsed ${logEntries.size} log entries")

        val uniqLogEntries = removeDuplicates(logEntries, gate.id)
        logger().info("Try to save ${uniqLogEntries.size} uniq log entries")
        logEntryRepository.saveAll(uniqLogEntries)

        return UploadLogEntriesInfo(
            totalSize = uniqLogEntries.size,
        )
    }

    private fun removeDuplicates(logEntries: List<LogEntry>, gateId: Long): List<LogEntry> {
        val existed = logEntryRepository.findAllLastMonthByGateId(gateId = gateId).map { it.uuid }.toSet()
        val uploaded = logEntries.map { it.uuid }.toSet()
        val duplicates = uploaded intersect existed
        logger().info("Uploaded ${logEntries.size}, unique -> ${(uploaded subtract existed).size}")
        val grouped = logEntries.associateBy { it.uuid }.toMutableMap()
        duplicates.forEach(grouped::remove)
        return grouped.values.toList()
    }

    fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int, filter: LogEntryFilter
    ) = logEntryRepository.findAllWithFilter(pageNum, pageSize, filter)

    fun getTop(
        gateId: Long,
        fieldFilter: LogEntryController.FieldFilter,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ) = logEntryRepository.getTop(gateId, fieldFilter, startDate, endDate)


    @Transactional
    fun removeByChecksum(checksum: String): Int {
        val size = logEntryRepository.countBySource(source = checksum)
        logger().info("Try to remove $size log entries for file $checksum")
        if (size > 0) logEntryRepository.removeBySource(source = checksum)
        return size
    }

    fun getAllLastNMonths(n: Int) = logEntryRepository.getAllLastNMonths(n)

    fun getOverview(phoneNumber: String): LogEntryOverview {
        val firstEntry = logEntryRepository.getFirstEntryByPhoneNumber(phoneNumber)
        if (firstEntry == null) throw IllegalArgumentException("Не найдены открытия шлагбаума или ворот с помощью телефона [$phoneNumber]")
        val logEntries = logEntryRepository.getLastEntriesByPhoneNumber(phoneNumber)
        val totalSize = logEntryRepository.countByPhoneNumber(phoneNumber)
        return LogEntryOverview(
            lastLogEntry = logEntries.first().toLogEntryResponse(),
            lastLogEntries = logEntries.map { it.toLogEntryResponse() }.subList(0, if (logEntries.size < 5) logEntries.size else 5),
            firstLogEntry = firstEntry.toLogEntryResponse(),
            totalSize = totalSize,
        )
    }
}