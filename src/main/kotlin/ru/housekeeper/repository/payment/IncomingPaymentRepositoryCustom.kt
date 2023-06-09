package ru.housekeeper.repository.payment

import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.filter.IncomingPaymentsFilter

interface IncomingPaymentRepositoryCustom {

    fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: IncomingPaymentsFilter
    ): Page<IncomingPayment>

}