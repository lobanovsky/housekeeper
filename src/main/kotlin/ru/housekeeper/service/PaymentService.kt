package ru.housekeeper.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.payment.CategoryOfPaymentEnum
import ru.housekeeper.enums.payment.GroupingPaymentByEnum
import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum
import ru.housekeeper.model.dto.AnnualPaymentVO
import ru.housekeeper.model.dto.MonthPaymentVO
import ru.housekeeper.model.dto.payment.GroupOfPayment
import ru.housekeeper.model.dto.payment.PaymentVO
import ru.housekeeper.model.dto.payment.toPaymentVO
import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.entity.payment.OutgoingPayment
import ru.housekeeper.model.entity.payment.Payment
import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingGropingPaymentsFilter
import ru.housekeeper.model.filter.OutgoingPaymentsFilter
import ru.housekeeper.parser.PaymentParser
import ru.housekeeper.repository.payment.IncomingPaymentRepository
import ru.housekeeper.repository.payment.OutgoingPaymentRepository
import ru.housekeeper.utils.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class PaymentService(
    private val incomingPaymentRepository: IncomingPaymentRepository,
    private val outgoingPaymentRepository: OutgoingPaymentRepository,
    private val counterpartyService: CounterpartyService,

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
        val uniqIncomingPayments = removeDuplicates(incomingPayments, "Incoming payment") { incomingPaymentRepository.findAllUUIDs().toSet() }
        val uniqOutgoingPayments = removeDuplicates(outgoingPayments, "Outgoing payment") { outgoingPaymentRepository.findAllUUIDs().toSet() }
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

    private fun removeDuplicates(payments: List<Payment>, description: String? = "", savedUUIDs: () -> Set<String>): List<Payment> {
        val saved = savedUUIDs()
        val uploaded = payments.map { it.uuid }.toSet()
        val duplicates = uploaded intersect saved
        logger().info("$description [${uploaded.size}], unique -> [${(uploaded subtract saved).size}]")
        val groupedPayments = payments.associateBy { it.uuid }.toMutableMap()
        duplicates.forEach(groupedPayments::remove)
        return groupedPayments.values.toList()
    }

    @Transactional
    @Synchronized
    fun parseAndSave(paymentsFile: MultipartFile, checksum: String): UploadFileInfo {
        val payments = PaymentParser(paymentsFile).parse()
        logger().info("Parsed ${payments.size} payments")
        return savePayments(payments, paymentsFile.originalFilename ?: paymentsFile.name, checksum)
    }

    fun findAllIncomingPaymentsWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: IncomingPaymentsFilter
    ): Page<IncomingPayment> =
        incomingPaymentRepository.findAllWithFilter(pageNum, pageSize, filter)

    fun findAllOutgoingPaymentsWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: OutgoingPaymentsFilter
    ): Page<OutgoingPayment> =
        outgoingPaymentRepository.findAllWithFilter(pageNum, pageSize, filter)

    fun findAllOutgoingGroupingPaymentsByCounterparty(filter: OutgoingGropingPaymentsFilter): List<GroupOfPayment> {
        val payments = outgoingPaymentRepository.findAllWithFilter(
            pageNum = 0,
            pageSize = MAX_SIZE_PER_PAGE_FOR_EXCEL,
            filter = OutgoingPaymentsFilter(
                startDate = filter.startDate,
                endDate = filter.endDate,
            )
        )
        val counterpartyMap = counterpartyService.findAll().associateBy { it.uuid }
        val groupOfPayment = mutableMapOf<String, GroupOfPayment>()
        for (payment in payments) {
            //skip self
            if (payment.toInn == myInn) continue
            //skip technical payment
            if (payment.purpose.contains("Перевод средств в связи с закрытием счета", true)) continue

            val counterparty = getCounterparty(counterpartyMap, payment)

            //custom rules
            val (key, name) = when (filter.groupBy) {
                GroupingPaymentByEnum.CATEGORY -> customRuleForPayments(payment, counterparty)
                GroupingPaymentByEnum.COUNTERPARTY -> Pair(counterparty.uuid, counterparty.name)
            }
            groupOfPayment[key] =
                groupOfPayment.getOrDefault(key, GroupOfPayment(name, mutableListOf(), BigDecimal.ZERO))
                    .addPayment(payment)
        }
        return groupOfPayment.values.toList().sortedByDescending { it.total }
    }

    private fun customRuleForPayments(payment: OutgoingPayment, counterparty: Counterparty): Pair<String, String> {

//        if (payment.purpose.contains("огнетушител", true))
//            return getPair(CategoryOfPaymentEnum.FIRE_SAFETY)
//
//        if (payment.purpose.contains("теплообменник", true))
//            return getPair(CategoryOfPaymentEnum.INDIVIDUAL_HEAT_POINT)
//
//        if (payment.purpose.contains("Аккумуляторные батареи для диспетчеризация", true))
//            return getPair(CategoryOfPaymentEnum.DISPATCHING)
//
//        if (payment.purpose.contains("Под отчет на приобретение посадочного материала", true))
//            return getPair(CategoryOfPaymentEnum.GARDEN)
//
//        if (payment.purpose.contains("ИД взыск", true))
//            return getPair(CategoryOfPaymentEnum.COURT_COSTS)
//
//        if (payment.toName.equals("Лобановский Евгений Владимирович", true)
//            && payment.purpose.contains("Оплата за предоставление услуг по договору с самозанятым", true)
//        ) return getPair(CategoryOfPaymentEnum.STAFF_SALARY)
//
//        if (payment.purpose.contains("Под отчёт", true) || payment.purpose.contains("Под отчет", true))
//            return getPair(CategoryOfPaymentEnum.UNDER_THE_REPORT)

        return Pair(
            counterparty.category.name,
            counterparty.category.description
        )
    }


    private fun getPair(category: CategoryOfPaymentEnum) = Pair(category.name, category.description)

    private fun getCounterparty(
        counterpartyGroupByUUID: Map<String, Counterparty>,
        payment: OutgoingPayment
    ): Counterparty {
        var counterpartyByName = counterpartyGroupByUUID[makeUUID(null, payment.toName)]
        if (counterpartyByName != null) return counterpartyByName
        counterpartyByName = counterpartyGroupByUUID[makeUUID(payment.toInn, payment.toName)]
        if (counterpartyByName != null) return counterpartyByName
        logger().info("Unknown counterparty: ${payment.id}, ${payment.purpose}, ${payment.toName}, ${payment.toInn}")
        val other = "Остальное"
        return Counterparty(
            uuid = other.onlyCyrillicLettersAndNumbers(),
            name = other,
            category = CategoryOfPaymentEnum.UNKNOWN,
            subcategory = CategoryOfPaymentEnum.UNKNOWN,
        )
    }

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
    fun removePaymentsByCheckSum(checksum: String): Int {
        val incomingSize = incomingPaymentRepository.countByPack(pack = checksum)
        val outgoingSize = outgoingPaymentRepository.countByPack(pack = checksum)
        logger().info("Try to remove $incomingSize incoming payments and $outgoingSize outgoing payments")
        if (incomingSize > 0) incomingPaymentRepository.removeByPack(pack = checksum)
        if (outgoingSize > 0) outgoingPaymentRepository.removeByPack(pack = checksum)
        return incomingSize + outgoingSize
    }

    fun setManualAccountForPayment(id: Long, account: String): IncomingPayment {
        val payment = incomingPaymentRepository.findByIdOrNull(id) ?: entityNotfound("входящий платёж" to id)
        return incomingPaymentRepository.save(payment.apply {
            this.account = account
            this.updateAccountDateTime = LocalDateTime.now()
            this.type = IncomingPaymentTypeEnum.MANUAL_ACCOUNT
        })
    }
}