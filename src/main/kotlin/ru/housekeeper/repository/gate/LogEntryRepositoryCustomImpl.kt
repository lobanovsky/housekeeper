package ru.housekeeper.repository.gate

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.gate.LogEntry
import ru.housekeeper.model.filter.LogEntryFilter
import ru.housekeeper.repository.equalFilterBy
import ru.housekeeper.repository.filterByDate
import ru.housekeeper.repository.likeFilterBy
import ru.housekeeper.utils.getPage

class LogEntryRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager
) : LogEntryRepositoryCustom {

    override fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int, filter: LogEntryFilter
    ): Page<LogEntry> {
        val predicates = mutableMapOf<String, String>()
        predicates["gateId"] = equalFilterBy("p.gateId", filter.gateId)
        predicates["phoneNumber"] = likeFilterBy("p.phoneNumber", filter.phoneNumber)
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

}