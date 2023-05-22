package ru.housekeeper.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.IncomingPayment
import ru.housekeeper.model.filter.IncomingPaymentsFilter

class IncomingPaymentRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : IncomingPaymentRepositoryCustom {

    override fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: IncomingPaymentsFilter
    ): Page<IncomingPayment> {
        val conditions = incomingFilters(filter)

        val sql = "SELECT p FROM IncomingPayment p WHERE true = true $conditions ORDER BY p.date DESC"
        val sqlCount = "SELECT count(p) FROM IncomingPayment p WHERE true = true $conditions"

        return getPage<IncomingPayment>(entityManager, sql, sqlCount, pageNum, pageSize)
    }

//    override fun findAnnualPayments(
//        annualPaymentsFilter: PaymentController.AnnualPaymentsFilter
//    ): List<IncomingPayment> {
//        val predicates = mutableMapOf<String, String>()
//        predicates["date"] = "EXTRACT(YEAR FROM p.date) = ${annualPaymentsFilter.year}"
//        predicates["fromInn"] = if (annualPaymentsFilter.excludeInns.isNotEmpty())
//            "AND p.fromInn NOT IN (${annualPaymentsFilter.excludeInns.sqlSeparator()})" else ""
//        predicates["flagged"] = if (annualPaymentsFilter.excludeFlagged.isNotEmpty())
//            "AND (p.flagged NOT IN (${annualPaymentsFilter.excludeFlagged.map { it.name }.sqlSeparator()}) OR p.flagged IS NULL)" else ""
//
//        val conditions = predicates.values.joinToString(separator = " ")
//
//        val sql = "SELECT p FROM IncomingPayment p WHERE $conditions ORDER BY p.date"
//
//        val query = entityManager.createQuery(sql, IncomingPayment::class.java)
//        return query.resultList
//    }

}