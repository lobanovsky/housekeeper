package ru.housekeeper.repository

import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.IncomingPayment
import ru.housekeeper.model.filter.IncomingPaymentsFilter

interface IncomingPaymentRepositoryCustom {

    fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: IncomingPaymentsFilter
    ): Page<IncomingPayment>

}