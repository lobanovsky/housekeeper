package ru.housekeeper.repository.payment

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.payment.OutgoingPayment
import ru.housekeeper.model.filter.OutgoingPaymentsFilter
import ru.housekeeper.utils.getPage

class OutgoingPaymentRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : OutgoingPaymentRepositoryCustom {

    override fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: OutgoingPaymentsFilter
    ): Page<OutgoingPayment> {
        val conditions = outgoingFilters(filter)

        val sql = "SELECT p FROM OutgoingPayment p WHERE true = true $conditions ORDER BY p.date DESC"
        val sqlCount = "SELECT count(p) FROM OutgoingPayment p WHERE true = true $conditions"

        return getPage<OutgoingPayment>(entityManager, sql, sqlCount, pageNum, pageSize)
    }

}