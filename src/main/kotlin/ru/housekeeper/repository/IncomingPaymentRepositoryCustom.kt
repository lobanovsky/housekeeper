package ru.housekeeper.repository

import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.IncomingPayment
import ru.housekeeper.model.filter.CompanyPaymentsFilter

interface IncomingPaymentRepositoryCustom {

    fun findAllFromCompanyByFilter(inn: String, pageNum: Int, pageSize: Int, filter: CompanyPaymentsFilter): Page<IncomingPayment>

}