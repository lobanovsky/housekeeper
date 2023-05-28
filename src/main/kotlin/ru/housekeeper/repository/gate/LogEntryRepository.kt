package ru.housekeeper.repository.gate

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.gate.LogEntry

@Repository
interface LogEntryRepository : CrudRepository<LogEntry, Long>, LogEntryRepositoryCustom {

    @Query("select l from LogEntry l where l.gateId = :gateId")
    fun findByGateId(
        @Param("gateId") gateId: Long
    ): List<LogEntry>
}