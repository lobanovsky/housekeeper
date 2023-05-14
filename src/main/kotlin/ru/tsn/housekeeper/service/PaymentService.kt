package ru.tsn.housekeeper.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.tsn.housekeeper.model.dto.AnnualPaymentVO
import ru.tsn.housekeeper.model.dto.MonthPaymentVO
import ru.tsn.housekeeper.model.dto.PaymentVO
import ru.tsn.housekeeper.model.entity.IncomingPayment
import ru.tsn.housekeeper.model.entity.OutgoingPayment
import ru.tsn.housekeeper.model.entity.Payment
import ru.tsn.housekeeper.model.filter.CompanyPaymentsFilter
import ru.tsn.housekeeper.parser.PaymentParser
import ru.tsn.housekeeper.repository.IncomingPaymentRepository
import ru.tsn.housekeeper.repository.OutgoingPaymentRepository
import ru.tsn.housekeeper.utils.logger
import ru.tsn.housekeeper.utils.sum
import ru.tsn.housekeeper.utils.toPaymentVO
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class PaymentService(
    private val incomingPaymentRepository: IncomingPaymentRepository,
    private val outgoingPaymentRepository: OutgoingPaymentRepository,
    private val fileService: FileService,

    ) {

    @Value("\${my.inn}")
    lateinit var myInn: String

    data class UploadFileInfo(
        val totalSize: Int,
        val uniqTotalSize: Int,
        val incomingSize: Int,
        val outgoingSize: Int,
        val incomingSum: BigDecimal?,
        val outgoingSum: BigDecimal?,
        val pack: String,
    )

    private fun savePayments(payments: List<PaymentVO>, fileName: String, checksum: String): UploadFileInfo {
        val incomingPayments = mutableListOf<IncomingPayment>()
        val outgoingPayments = mutableListOf<OutgoingPayment>()
        val now = LocalDateTime.now()
        for (payment in payments) {
            if (payment.incomingSum != null) {
                incomingPayments.add(payment.toIncomingPayment(now, fileName, checksum))
            }
            if (payment.outgoingSum != null) {
                outgoingPayments.add(payment.toOutgoingPayment(now, fileName, checksum))
            }
        }
        val uniqIncomingPayments =
            removeDuplicates(incomingPayments) { incomingPaymentRepository.findAllUUIDs().toSet() }
        val uniqOutgoingPayments =
            removeDuplicates(outgoingPayments) { outgoingPaymentRepository.findAllUUIDs().toSet() }
        logger().info("Try to save ${uniqIncomingPayments.size} incoming payments in the amount: ${uniqIncomingPayments.sum()}")
        logger().info("Try to save ${uniqOutgoingPayments.size} outgoing payments in the amount: ${uniqOutgoingPayments.sum()}")
        incomingPaymentRepository.saveAll(uniqIncomingPayments)
        outgoingPaymentRepository.saveAll(uniqOutgoingPayments)
        return UploadFileInfo(
            totalSize = payments.size,
            uniqTotalSize = uniqIncomingPayments.size + uniqOutgoingPayments.size,
            incomingSize = uniqIncomingPayments.size,
            outgoingSize = uniqOutgoingPayments.size,
            incomingSum = uniqIncomingPayments.sum(),
            outgoingSum = uniqOutgoingPayments.sum(),
            pack = checksum
        )
    }

    private fun removeDuplicates(payments: List<Payment>, savedUUIDs: () -> Set<String>): List<Payment> {
        val saved = savedUUIDs()
        val uploaded = payments.map { it.uuid }.toSet()
        val duplicates = uploaded intersect saved
        logger().info("Uploaded ${uploaded.size}, unique -> ${(uploaded subtract saved).size}")
        val groupedPayments = payments.associateBy { it.uuid }.toMutableMap()
        duplicates.forEach(groupedPayments::remove)
        return groupedPayments.values.toList()
    }

    fun parseAndSave(paymentsFile: MultipartFile, checksum: String): UploadFileInfo {
        val payments = PaymentParser(paymentsFile).parse()
        logger().info("Parsed ${payments.size} payments")
        return savePayments(payments, paymentsFile.originalFilename ?: paymentsFile.name, checksum)
    }

    fun findAllFromCompanyByFilter(
        inn: String,
        pageNum: Int,
        pageSize: Int,
        filter: CompanyPaymentsFilter
    ): Page<IncomingPayment> =
        incomingPaymentRepository.findAllFromCompanyByFilter(inn, pageNum, pageSize, filter)

    fun findAllOutgoingPaymentsByFilter(i: Int, pageNum: Int, filter: CompanyPaymentsFilter): Page<OutgoingPayment> =
        outgoingPaymentRepository.findAllOutgoingPaymentsByFilter(i, pageNum, filter)

    fun findAllDeposits(): List<OutgoingPayment> = outgoingPaymentRepository.findAllDeposits(myInn)

    fun findAnnualPayments(year: Int): AnnualPaymentVO = AnnualPaymentVO(
        year = year,
        totalSum = incomingPaymentRepository.getTotalSumByYear(year),
        depositSum = incomingPaymentRepository.getDepositSumByYear(year),
        taxableSum = incomingPaymentRepository.getTaxableSumByYear(year),
        taxFreeSum = incomingPaymentRepository.getTaxFreeSumByYear(year),
        taxablePaymentsByMonths = groupByMonth(incomingPaymentRepository.findAllTaxableByYear(year)),
        taxFreePaymentsByMonths = groupByMonth(incomingPaymentRepository.findAllTaxFreeByYear(year)),
        depositsPaymentsByMonths = groupByMonth(incomingPaymentRepository.findAllDepositsByYear(year))
    )

    private fun groupByMonth(payments: List<IncomingPayment>): List<MonthPaymentVO> {
        val groupedPayments = payments.groupBy { it.date.month }
        return groupedPayments.map { (month, payments) ->
            MonthPaymentVO(
                month = month,
                numberOfMonth = month.value,
                size = payments.size,
                payments = payments.map { it.toPaymentVO(incomingSum = it.sum) },
                totalSum = payments.sum()
            )
        }
    }

    @Transactional
    fun removePaymentsByCheckSum(fileId: Long, checksum: String): Int {
        fileService.deleteByid(fileId)
        val incomingSize = incomingPaymentRepository.countByPack(pack = checksum)
        val outgoingSize = outgoingPaymentRepository.countByPack(pack = checksum)
        logger().info("Try to remove $incomingSize incoming payments and $outgoingSize outgoing payments")
        if (incomingSize > 0) incomingPaymentRepository.removeByPack(pack = checksum)
        if (outgoingSize > 0) outgoingPaymentRepository.removeByPack(pack = checksum)
        return incomingSize + outgoingSize
    }
}