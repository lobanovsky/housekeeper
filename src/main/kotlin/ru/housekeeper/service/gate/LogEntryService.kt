package ru.housekeeper.service.gate

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.entity.gate.LogEntry
import ru.housekeeper.model.filter.LogEntryFilter
import ru.housekeeper.parser.gate.LogEntryParser
import ru.housekeeper.repository.gate.LogEntryRepository
import ru.housekeeper.utils.logger

@Service
class LogEntryService(

    private val gateService: GateService,
    private val logEntryRepository: LogEntryRepository,

    ) {

    data class UploadLogEntriesInfo(
        val totalSize: Int,
    )

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
        val existed = logEntryRepository.findByGateId(gateId = gateId).map { it.uuid }.toSet()
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

}