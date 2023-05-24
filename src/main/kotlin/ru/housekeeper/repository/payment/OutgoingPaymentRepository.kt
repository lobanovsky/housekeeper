package ru.housekeeper.repository.payment

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.OutgoingPayment
import ru.housekeeper.model.entity.Payment

@Repository
interface OutgoingPaymentRepository : CrudRepository<Payment, Long>, OutgoingPaymentRepositoryCustom {

    @Query("SELECT p.uuid FROM OutgoingPayment p")
    fun findAllUUIDs(): List<String>

    @Query("SELECT p FROM OutgoingPayment p WHERE p.toInn = :toInn AND p.purpose LIKE '%депозит%'")
    fun findAllDeposits(@Param("toInn") toInn: String): List<OutgoingPayment>

    @Modifying
    @Query("DELETE FROM OutgoingPayment p WHERE p.pack = :pack")
    fun removeByPack(@Param("pack") pack: String)

    @Query("SELECT COUNT(p) FROM OutgoingPayment p WHERE p.pack = :pack")
    fun countByPack(@Param("pack") pack: String): Int

}