package ru.housekeeper.repository.gate

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.gate.Gate

@Repository
interface GateRepository : CrudRepository<Gate, Long> {

    @Query("SELECT g FROM Gate g WHERE g.imei = :imei")
    fun findByImei(
        @Param("imei") imei: String
    ): Gate?

}