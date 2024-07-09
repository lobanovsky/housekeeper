package ru.housekeeper.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.repository.payment.IncomingPaymentRepository
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.time.LocalDate

@Service
class RepairService(
    private val paymentService: PaymentService,
    private val paymentRepository: IncomingPaymentRepository,
) {

    @Transactional
    fun findAndRemoveDuplicates() {
        val incomingPayments = paymentService.findAllIncomingPaymentsWithFilter(0, 10000, IncomingPaymentsFilter())
        val duplicatePayments = findIds(incomingPayments.content)
        logger().info("Remove all duplicate payments with ids = $duplicatePayments")
        paymentRepository.deleteByIds(duplicatePayments)
    }

    //find duplicate payments by uuid: date without time + docNumber + sum
    private fun findIds(payments: List<IncomingPayment>): List<Long> {
        val uuids = mutableSetOf<Pair<Long, String>>()
        payments.forEach {
            uuids.add(Pair(it.id ?: 0, "${it.date.toLocalDate()} ${it.docNumber} ${it.sum}"))
        }
        val groupByUUID = uuids.groupBy { it.second }
        val countOfDuplicates = payments.size - groupByUUID.size
        if (countOfDuplicates == 0) {
            logger().info("No duplicates found")
            return emptyList()
        }
        logger().info("IncomingPayments size = ${payments.size}, Unique UUIDs size = ${groupByUUID.size}, Duplicate UUIDs size = $countOfDuplicates")

        //show grouped incoming payments where values size more than one
        val groupById = payments.groupBy { it.id }
        val idsForRemove = mutableListOf<Long?>()
        var i = 1
        groupByUUID.filter { it.value.size > 1 }.forEach { it ->
            //show id, uuid, date, doc_number, sum, source for values
            logger().info("$i-------------------------------------------------")
            it.value.forEach {
                val payment = groupById[it.first]?.get(0)
                logger().info("id=${payment?.id}, account = ${payment?.account} uuid=${payment?.uuid}, date=${payment?.date}, doc_number=${payment?.docNumber}, sum=${payment?.sum}, source=${payment?.source}")
            }
            i++
            //get all values except first
            it.value.drop(1).forEach { idsForRemove.add(it.first) }
        }
        return idsForRemove.filterNotNull()
    }

    @Transactional
    fun updateUUID() {
        val incomingPayments = paymentService.findAllIncomingPaymentsWithFilter(0, 10000, IncomingPaymentsFilter())
        incomingPayments.content.forEach {
            it.uuid = "${it.date.toLocalDate()} ${it.docNumber} ${it.sum}"
        }
        logger().info("Update UUID for incoming payments, size = ${incomingPayments.content.size}")
    }

    fun getSumOfPayments(
        startDate: LocalDate, endDate:
        LocalDate, toAccounts: List<String>
    ): BigDecimal {
        val incomingPayments = paymentService.findAllIncomingPaymentsWithFilter(
            0, 10000, IncomingPaymentsFilter(
                startDate = startDate,
                endDate = endDate,
                toAccounts = toAccounts
            )
        )
        return incomingPayments.content
            .filterNot { it.purpose.contains("Возврат депозита по договору") }
            .map { it.sum }
            .fold(BigDecimal.ZERO, BigDecimal::add)
    }
}