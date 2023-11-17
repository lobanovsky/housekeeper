package ru.housekeeper.repository.payment

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.entity.payment.Payment
import java.math.BigDecimal

@Repository
interface IncomingPaymentRepository : CrudRepository<Payment, Long>, IncomingPaymentRepositoryCustom {

    @Query("SELECT p.uuid FROM IncomingPayment p")
    fun findAllUUIDs(): List<String>

    //total
    @Query("SELECT SUM(p.sum) FROM IncomingPayment p WHERE EXTRACT(YEAR FROM p.date) = :year")
    fun getTotalSumByYear(@Param("year") year: Int): BigDecimal

    //deposits
    @Query("SELECT SUM(p.sum) FROM IncomingPayment p WHERE EXTRACT(YEAR FROM p.date) = :year AND p.type IN (:types)")
    fun getDepositSumByYear(
        @Param("year") year: Int,
        @Param("types") types: Set<IncomingPaymentTypeEnum> = setOf(
            IncomingPaymentTypeEnum.DEPOSIT_PERCENTAGES,
            IncomingPaymentTypeEnum.DEPOSIT_REFUND
        )
    ): BigDecimal?

    @Query("SELECT p FROM IncomingPayment p WHERE EXTRACT(YEAR FROM p.date) = :year AND p.type IN (:types) ORDER BY p.date")
    fun findAllDepositsByYear(
        @Param("year") year: Int,
        @Param("types") types: Set<IncomingPaymentTypeEnum> = setOf(
            IncomingPaymentTypeEnum.DEPOSIT_PERCENTAGES,
            IncomingPaymentTypeEnum.DEPOSIT_REFUND
        )
    ): List<IncomingPayment>

    //taxable, payments from company for the services
    @Query("SELECT SUM(p.sum) FROM IncomingPayment p WHERE EXTRACT(YEAR FROM p.date) = :year AND p.type IN (:types)")
    fun getTaxableSumByYear(
        @Param("year") year: Int,
        @Param("types") types: Set<IncomingPaymentTypeEnum> = setOf(IncomingPaymentTypeEnum.TAXABLE)
    ): BigDecimal

    @Query("SELECT p FROM IncomingPayment p WHERE EXTRACT(YEAR FROM p.date) = :year AND p.type IN (:types) ORDER BY p.date")
    fun findAllTaxableByYear(
        @Param("year") year: Int,
        @Param("types") types: Set<IncomingPaymentTypeEnum> = setOf(IncomingPaymentTypeEnum.TAXABLE)
    ): List<IncomingPayment>

    //tax-free without taxable and deposits
    @Query("SELECT SUM(p.sum) FROM IncomingPayment p WHERE EXTRACT(YEAR FROM p.date) = :year AND (p.type NOT IN (:types))")
    fun getTaxFreeSumByYear(
        @Param("year") year: Int,
        @Param("types") types: Set<IncomingPaymentTypeEnum> = setOf(
            IncomingPaymentTypeEnum.TAXABLE,
            IncomingPaymentTypeEnum.DEPOSIT_PERCENTAGES,
            IncomingPaymentTypeEnum.DEPOSIT_REFUND
        )
    ): BigDecimal

    @Query("SELECT p FROM IncomingPayment p WHERE EXTRACT(YEAR FROM p.date) = :year AND (p.type NOT IN (:types)) ORDER BY p.date")
    fun findAllTaxFreeByYear(
        @Param("year") year: Int,
        @Param("types") types: Set<IncomingPaymentTypeEnum> = setOf(
            IncomingPaymentTypeEnum.TAXABLE,
            IncomingPaymentTypeEnum.DEPOSIT_PERCENTAGES,
            IncomingPaymentTypeEnum.DEPOSIT_REFUND
        )
    ): List<IncomingPayment>

    @Modifying
    @Query("DELETE FROM IncomingPayment p WHERE p.pack = :pack")
    fun removeByPack(@Param("pack") pack: String)

    @Query("SELECT COUNT(p) FROM IncomingPayment p WHERE p.pack = :pack")
    fun countByPack(@Param("pack") pack: String): Int

    @Query("SELECT p FROM IncomingPayment p WHERE p.toAccount IN (:toAccounts) and (p.type != 'ACCOUNT' or p.type IS NULL )")
    fun findByToAccountsAndAccountIsNull(
        toAccounts: Set<String>,
    ): List<IncomingPayment>
}