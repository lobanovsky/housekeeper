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
import java.time.LocalDate

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
                        LogEntryController.FieldFilter.FLAT_NUMBER -> it[2] as String
                        LogEntryController.FieldFilter.PHONE_NUMBER -> null
                    },
                    phoneNumber = when (fieldFilter) {
                        LogEntryController.FieldFilter.FLAT_NUMBER -> null
                        LogEntryController.FieldFilter.PHONE_NUMBER -> it[2] as String
                    },
                    userName = when (fieldFilter) {
                        LogEntryController.FieldFilter.FLAT_NUMBER -> null
                        LogEntryController.FieldFilter.PHONE_NUMBER -> it[2] as String
                    }
                )
            }
    }

}