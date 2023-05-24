package ru.housekeeper.repository.payment

import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.OutgoingPayment
import ru.housekeeper.model.filter.OutgoingPaymentsFilter

interface OutgoingPaymentRepositoryCustom {

    fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: OutgoingPaymentsFilter
    ): Page<OutgoingPayment>

}