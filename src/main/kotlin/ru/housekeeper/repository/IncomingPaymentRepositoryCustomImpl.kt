package ru.housekeeper.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
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
        val predicates = mutableMapOf<String, String>()
        predicates["fromInn"] = if (filter.fromInn?.isNotEmpty() == true)
            "AND LOWER(p.fromInn) LIKE '%${filter.fromInn.lowercase().trim()}%'" else ""
        predicates["fromName"] = if (filter.fromName?.isNotEmpty() == true)
            "AND LOWER(p.fromName) LIKE '%${filter.fromName.lowercase().trim()}%'" else ""
        predicates["purpose"] = if (filter.purpose?.isNotEmpty() == true)
            "AND LOWER(p.purpose) LIKE '%${filter.purpose.lowercase().trim()}%'" else ""
        predicates["taxable"] = if (filter.taxable == true) "AND p.taxable = true" else ""
        predicates["date"] =
            if (filter.startDate != null && filter.endDate != null) "AND (cast(p.date as date) BETWEEN '${filter.startDate}' AND '${filter.endDate}')" else ""

        val conditions = predicates.values.joinToString(separator = " ")

        val sql = "SELECT p FROM IncomingPayment p WHERE true = true $conditions ORDER BY p.date DESC"
        val sqlCount = "SELECT count(p) FROM IncomingPayment p WHERE true = true $conditions"

        val query = entityManager.createQuery(sql, IncomingPayment::class.java)
        val pageable: Pageable = PageRequest.of(pageNum, pageSize)
        query.firstResult = pageSize * pageNum
        query.maxResults = pageable.pageSize
        return PageableExecutionUtils.getPage(
            query.resultList,
            pageable
        ) { entityManager.createQuery(sqlCount).resultList[0] as Long }
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