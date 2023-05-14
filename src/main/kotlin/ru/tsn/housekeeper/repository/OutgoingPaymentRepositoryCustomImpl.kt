package ru.tsn.housekeeper.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import ru.tsn.housekeeper.model.entity.OutgoingPayment
import ru.tsn.housekeeper.model.filter.CompanyPaymentsFilter

class OutgoingPaymentRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : OutgoingPaymentRepositoryCustom {

    override fun findAllOutgoingPaymentsByFilter(
        pageNum: Int,
        pageSize: Int,
        filter: CompanyPaymentsFilter
    ): Page<OutgoingPayment> {
        val predicates = mutableMapOf<String, String>()
        predicates["toName"] = if (filter.toName?.isNotEmpty() == true) "AND LOWER(p.toName) LIKE '%${filter.toName.lowercase().trim()}%'" else ""
        predicates["fromName"] = if (filter.fromName?.isNotEmpty() == true) "AND LOWER(p.fromName) LIKE '%${filter.fromName.lowercase().trim()}%'" else ""
        predicates["purpose"] = if (filter.purpose?.isNotEmpty() == true) "AND LOWER(p.purpose) LIKE '%${filter.purpose.lowercase().trim()}%'" else ""
        predicates["taxable"] = if (filter.taxable == true) "AND p.taxable = true" else ""
        predicates["date"] = if (filter.startDate != null && filter.endDate != null) "AND (cast(p.date as date) BETWEEN '${filter.startDate}' AND '${filter.endDate}')" else ""

        val conditions = predicates.values.joinToString(separator = " ")

        val sql = "SELECT p FROM OutgoingPayment p WHERE true = true $conditions ORDER BY p.date"
        val sqlCount = "SELECT count(p) FROM OutgoingPayment p WHERE true = true $conditions"

        val query = entityManager.createQuery(sql, OutgoingPayment::class.java)
        val pageable: Pageable = PageRequest.of(pageNum, pageSize)
        query.firstResult = pageSize * pageNum
        query.maxResults = pageable.pageSize
        return PageableExecutionUtils.getPage(query.resultList, pageable) { entityManager.createQuery(sqlCount).resultList[0] as Long }
    }

}