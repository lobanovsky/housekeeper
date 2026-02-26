package ru.housekeeper.service.gate

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import ru.housekeeper.model.entity.gate.Gate
import ru.housekeeper.model.entity.gate.LogEntry
import ru.housekeeper.repository.gate.LogEntryRepository
import ru.housekeeper.service.gate.EldesApiClient.EventLogEntryResponse
import ru.housekeeper.utils.logger
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class GateSyncService(
    private val gateService: GateService,
    private val logEntryRepository: LogEntryRepository,
    private val eldesApiClient: EldesApiClient,
) {

    companion object {
        private const val PAGE_SIZE = 500
        private const val SOURCE = "eldes-api-sync"
    }

    data class GateSyncResult(
        val gateId: Long,
        val gateName: String,
        val fromDate: LocalDate?,
        val fetched: Int,
        val saved: Int,
    )

    @Scheduled(cron = "0 0 4 * * *")
    fun scheduledSync() {
        logger().info("Starting scheduled eldes-api sync")
        syncAll()
    }

    fun syncAll(): List<GateSyncResult> =
        gateService.getAllGates().map { syncGate(it) }

    fun syncGate(gateId: Long): GateSyncResult {
        val gate = gateService.getAllGates().firstOrNull { it.id == gateId }
            ?: throw IllegalArgumentException("Gate $gateId not found")
        return syncGate(gate)
    }

    private fun syncGate(gate: Gate): GateSyncResult {
        val lastEntry = logEntryRepository.findTopByGateIdOrderByDateTimeDesc(gate.id)
        val fromDate = lastEntry?.dateTime?.toLocalDate()?.minusDays(1)

        logger().info("Syncing gate ${gate.id} '${gate.name}' (deviceId=${gate.deviceId}) from=$fromDate")

        val entries = fetchAllPages(gate.deviceId, fromDate)
        logger().info("Fetched ${entries.size} entries from eldes-api for gate ${gate.id}")

        val logEntries = entries.mapNotNull { toLogEntry(it, gate) }
        val unique = removeDuplicates(logEntries, gate.id)
        logEntryRepository.saveAll(unique)

        logger().info("Saved ${unique.size} new entries for gate ${gate.id}")
        return GateSyncResult(
            gateId = gate.id,
            gateName = gate.name,
            fromDate = fromDate,
            fetched = entries.size,
            saved = unique.size,
        )
    }

    private fun fetchAllPages(deviceId: String, from: LocalDate?): List<EventLogEntryResponse> {
        val all = mutableListOf<EventLogEntryResponse>()
        var page = 0
        while (true) {
            val response = eldesApiClient.fetchEventLog(deviceId, from, page, PAGE_SIZE)
            all.addAll(response.entries)
            if (page >= response.totalPages - 1) break
            page++
        }
        return all
    }

    private fun toLogEntry(entry: EventLogEntryResponse, gate: Gate): LogEntry? {
        val status = runCatching { LogEntryStatusEnum.valueOf(entry.status) }.getOrElse {
            logger().warn("Unknown status '${entry.status}' from eldes-api, skipping entry")
            return null
        }
        val method = entry.method?.let { LogEntryAccessMethodEnum.fromString(it) }
        val identifier = when (status) {
            LogEntryStatusEnum.OPENED,
            LogEntryStatusEnum.AUTH_FAILED,
            LogEntryStatusEnum.USER_ADDED -> entry.phoneNumber ?: ""
            LogEntryStatusEnum.NUM_DELETED -> entry.cell ?: ""
            LogEntryStatusEnum.UNDEFINED -> entry.line ?: ""
        }
        val uuid = makeUUID(entry.dateTime, identifier, gate)
        val flatNumber = entry.userName?.let { extractFlatNumber(it) }

        return LogEntry(
            source = SOURCE,
            gateId = gate.id,
            gateName = gate.name,
            dateTime = entry.dateTime,
            status = status,
            userName = entry.userName,
            flatNumber = flatNumber,
            cell = entry.cell,
            method = method,
            phoneNumber = entry.phoneNumber,
            line = entry.line,
            uuid = uuid,
            deviceId = gate.deviceId,
        )
    }

    private fun makeUUID(dateTime: LocalDateTime, identifier: String, gate: Gate) =
        "${dateTime.year}_${dateTime.monthValue}_${dateTime.dayOfMonth}_${dateTime.hour}_${dateTime.minute}_${dateTime.second}_${identifier}_gate_${gate.id}_${gate.id}"

    private fun extractFlatNumber(userName: String): String {
        val parts = userName.split("-")
        if (parts.size == 2 && parts[0].matches(Regex("\\d+"))) {
            return parts[0]
        }
        return userName
    }

    private fun removeDuplicates(logEntries: List<LogEntry>, gateId: Long): List<LogEntry> {
        val existed = logEntryRepository.findAllLastMonthByGateId(gateId = gateId).map { it.uuid }.toSet()
        val grouped = logEntries.associateBy { it.uuid }.toMutableMap()
        existed.forEach(grouped::remove)
        logger().info("Deduplication: ${logEntries.size} fetched, ${grouped.size} unique (not in last 3 months)")
        return grouped.values.toList()
    }
}
