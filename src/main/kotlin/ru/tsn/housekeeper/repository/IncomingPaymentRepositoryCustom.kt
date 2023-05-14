package ru.tsn.housekeeper.repository

import org.springframework.data.domain.Page
import ru.tsn.housekeeper.model.entity.IncomingPayment
import ru.tsn.housekeeper.model.filter.CompanyPaymentsFilter

interface IncomingPaymentRepositoryCustom {

    fun findAllFromCompanyByFilter(inn: String, pageNum: Int, pageSize: Int, filter: CompanyPaymentsFilter): Page<IncomingPayment>

}