package ru.housekeeper.repository.gate

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import ru.housekeeper.model.entity.gate.LogEntry
import ru.housekeeper.model.filter.LogEntryFilter
import ru.housekeeper.repository.equalFilterBy
import ru.housekeeper.repository.filterByDate
import ru.housekeeper.repository.likeFilterBy
import ru.housekeeper.rest.gate.LogEntryController
import ru.housekeeper.utils.getPage
import ru.housekeeper.utils.onlyNumbers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class LogEntryRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager
) : LogEntryRepositoryCustom {

    override fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int, filter: LogEntryFilter
    ): Page<LogEntry> {
        val predicates = mutableMapOf<String, String>()
        predicates["gateId"] = equalFilterBy("p.gateId", filter.gateId)
        predicates["phoneNumber"] = likeFilterBy("p.phoneNumber", filter.phoneNumber?.onlyNumbers())
        predicates["userName"] = likeFilterBy("p.userName", filter.userName)
        predicates["flatNumber"] = equalFilterBy("p.flatNumber", filter.flatNumber)
        predicates["status"] = equalFilterBy("p.status", filter.status)
        predicates["method"] = equalFilterBy("p.method", filter.method)
        predicates["date"] = filterByDate("cast(p.dateTime as date)", filter.startDate, filter.endDate)
        val conditions = predicates.values.joinToString(separator = " ")

        val sql = "SELECT p FROM LogEntry p WHERE true = true $conditions ORDER BY p.dateTime DESC"
        val sqlCount = "SELECT count(p) FROM LogEntry p WHERE true = true $conditions"

        return getPage<LogEntry>(entityManager, sql, sqlCount, pageNum, pageSize)
    }

    override fun getTop(
        gateId: Long,
        fieldFilter: LogEntryController.FieldFilter,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): List<LogEntryRepositoryCustom.TopRatingResponse> {
        val predicates = mutableMapOf<String, String>()
        predicates["gateId"] = equalFilterBy("p.gateId", gateId)
        predicates["status"] = equalFilterBy("p.status", LogEntryStatusEnum.OPENED)
        predicates["date"] = filterByDate("cast(p.dateTime as date)", startDate, endDate)
        val conditions = predicates.values.joinToString(separator = " ")

        val sql = when (fieldFilter) {
            LogEntryController.FieldFilter.FLAT_NUMBER ->
                "SELECT COUNT(p.flatNumber) AS cnt, p.flatNumber FROM LogEntry p WHERE true = true $conditions GROUP BY p.flatNumber ORDER BY cnt DESC"

            LogEntryController.FieldFilter.PHONE_NUMBER ->
                "SELECT COUNT(p.phoneNumber) AS cnt, p.phoneNumber, p.userName FROM LogEntry p WHERE true = true $conditions GROUP BY p.phoneNumber, p.userName ORDER BY cnt DESC"
        }

        return entityManager.createQuery(sql).resultList
            .map { it as Array<*> }
            .map {
                LogEntryRepositoryCustom.TopRatingResponse(
                    count = it[0] as Long,
                    flatNumber = when (fieldFilter) {
                        LogEntryController.FieldFilter.FLAT_NUMBER -> it[1] as String
                        LogEntryController.FieldFilter.PHONE_NUMBER -> null
                    },
                    phoneNumber = when (fieldFilter) {
                        LogEntryController.FieldFilter.FLAT_NUMBER -> null
                        LogEntryController.FieldFilter.PHONE_NUMBER -> it[1] as String
                    },
                    userName = when (fieldFilter) {
                        LogEntryController.FieldFilter.FLAT_NUMBER -> null
                        LogEntryController.FieldFilter.PHONE_NUMBER -> it[2] as String
                    }
                )
            }
    }

    override fun getAllLastNMonths(n: Int): List<LogEntry> {
        //now minus n months
        val startDate = LocalDateTime.now().minus(n.toLong(), ChronoUnit.MONTHS)

        val sql = "SELECT p FROM LogEntry p WHERE p.dateTime >= :startDate ORDER BY p.dateTime DESC"
        val query = entityManager.createQuery(sql, LogEntry::class.java)
        query.setParameter("startDate", startDate)
        return query.resultList as List<LogEntry>
    }

    override fun getLastEntriesByPhoneNumber(phoneNumber: String, n: Int): List<LogEntry> {
        val sql = "SELECT p FROM LogEntry p WHERE p.phoneNumber = :phoneNumber ORDER BY p.dateTime DESC"
        val query = entityManager.createQuery(sql, LogEntry::class.java)
        query.setParameter("phoneNumber", phoneNumber)
        query.maxResults = n
        return query.resultList as List<LogEntry>
    }

    override fun getFirstEntryByPhoneNumber(phoneNumber: String): LogEntry? {
        val sql = "SELECT p FROM LogEntry p WHERE p.phoneNumber = :phoneNumber ORDER BY p.dateTime ASC"
        val query = entityManager.createQuery(sql, LogEntry::class.java)
        query.setParameter("phoneNumber", phoneNumber)
        query.maxResults = 1
        val result = query.resultList as List<LogEntry>
        return if (result.isEmpty()) null else result[0]
    }

    override fun findAllLastMonthByGateId(gateId: Long): List<LogEntry> {
        val startDate = LocalDateTime.now().minus(1, ChronoUnit.MONTHS)
        val sql = "SELECT p FROM LogEntry p WHERE p.gateId = :gateId AND p.dateTime >= :startDate ORDER BY p.dateTime DESC"
        val query = entityManager.createQuery(sql, LogEntry::class.java)
        query.setParameter("gateId", gateId)
        query.setParameter("startDate", startDate)
        return query.resultList as List<LogEntry>
    }
}