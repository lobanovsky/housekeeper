package ru.housekeeper.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Decision

@Repository
interface DecisionRepository : CrudRepository<Decision, Long> {

    @Query("SELECT d FROM Decision d WHERE d.print = :print")
    fun findByPrint(
        @Param("print") print: Boolean
    ): List<Decision>

    @Query("SELECT d FROM Decision d WHERE d.voted = :voted")
    fun findByVoted(
        @Param("voted") voted: Boolean
    ): List<Decision>

    @Query("SELECT d FROM Decision d WHERE d.notified = :notified AND d.voted = :voted")
    fun findByNotifiedAndVoted(
        @Param("notified") notified: Boolean,
        @Param("voted") voted: Boolean
    ): List<Decision>

    @Query("SELECT d FROM Decision d WHERE d.voted = false order by d.percentage desc")
    fun findNotVoted(): List<Decision>

    @Query("SELECT COUNT(d) FROM Decision d")
    fun countDecisions(): Long

}