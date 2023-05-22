package ru.housekeeper.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import ru.housekeeper.model.entity.OutgoingPayment
import ru.housekeeper.model.filter.OutgoingPaymentsFilter

class OutgoingPaymentRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : OutgoingPaymentRepositoryCustom {

    override fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: OutgoingPaymentsFilter
    ): Page<OutgoingPayment> {
        val predicates = mutableMapOf<String, String>()
        predicates["toName"] = filterBy("p.toName", filter.toName)
        predicates["toInn"] = filterBy("p.toInn", filter.toInn)
        predicates["purpose"] = filterBy("p.purpose", filter.purpose)
        predicates["taxable"] = filterBy("p.taxable", filter.taxable)
        predicates["date"] = filterByDate("(cast(p.date as date)", filter.startDate, filter.endDate)

        val conditions = predicates.values.joinToString(separator = " ")

        val sql = "SELECT p FROM OutgoingPayment p WHERE true = true $conditions ORDER BY p.date DESC"
        val sqlCount = "SELECT count(p) FROM OutgoingPayment p WHERE true = true $conditions"

        val query = entityManager.createQuery(sql, OutgoingPayment::class.java)
        val pageable: Pageable = PageRequest.of(pageNum, pageSize)
        query.firstResult = pageSize * pageNum
        query.maxResults = pageable.pageSize
        return PageableExecutionUtils.getPage(
            query.resultList,
            pageable
        ) { entityManager.createQuery(sqlCount).resultList[0] as Long }
    }

}