package ru.housekeeper.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Counterparty

@Repository
interface CounterpartyRepository : CrudRepository<Counterparty, Long> {

    @Query("SELECT p.uuid FROM Counterparty p")
    fun findAllUUIDs(): List<String>

}
