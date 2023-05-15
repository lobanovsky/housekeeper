package ru.housekeeper.repository

import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.OutgoingPayment
import ru.housekeeper.model.filter.CompanyPaymentsFilter

interface OutgoingPaymentRepositoryCustom {

    fun findAllOutgoingPaymentsByFilter(pageNum: Int, pageSize: Int, filter: CompanyPaymentsFilter): Page<OutgoingPayment>

}