package ru.tsn.housekeeper.repository

import org.springframework.data.domain.Page
import ru.tsn.housekeeper.model.entity.OutgoingPayment
import ru.tsn.housekeeper.model.filter.CompanyPaymentsFilter

interface OutgoingPaymentRepositoryCustom {

    fun findAllOutgoingPaymentsByFilter(pageNum: Int, pageSize: Int, filter: CompanyPaymentsFilter): Page<OutgoingPayment>

}