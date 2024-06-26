package ru.housekeeper.service.registry

import org.springframework.stereotype.Service
import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum
import ru.housekeeper.enums.registry.RegistryChannelEnum
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.entity.registry.RegistryLastLine
import ru.housekeeper.model.entity.registry.RegistryRow
import ru.housekeeper.model.entity.registry.RegistrySum
import ru.housekeeper.model.filter.IncomingPaymentsFilter
import ru.housekeeper.service.PaymentService
import ru.housekeeper.utils.MAX_SIZE_PER_PAGE_FOR_EXCEL
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class RegistryService(
    private val ruleService: RuleService,
    private val paymentService: PaymentService,
) {

    fun makeCustom(sum: String): List<String> {
        //8195,41
        val p01 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000004165", fromName = "Безмен Виктор Георгиевич", sum = BigDecimal("4338.61"))
        val p02 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700077", fromName = "Скалозуб Виктория Николаевна", sum = BigDecimal("358.94"))
        val p03 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700135", fromName = "Коковина Ольга Ивановна", sum = BigDecimal("296.31"))
        val p04 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700137", fromName = "Комбаров Александр Анатольевич", sum = BigDecimal("607.06"))
        val p05 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700091", fromName = "Коковина Ольга Ивановна", sum = BigDecimal("301.13"))
        val p06 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700098", fromName = "Романовский Геннадий Георгиевич", sum = BigDecimal("722.70"))
        val p07 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700109", fromName = "Липецкий Иван Владимирович", sum = BigDecimal("917.82"))
        val p08 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700134", fromName = "Коковина Ольга Ивановна", sum = BigDecimal("293.90"))
        val p09 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700075", fromName = "Бессараб И. В.", sum = BigDecimal("358.94"))
        //24985,05
        val p10 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700129", fromName = "Гогохия Д.Г.", sum = BigDecimal("22631.47"))
        val p11 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700117", fromName = "Гущин С.В.", sum = BigDecimal("717.88"))
        val p12 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700118", fromName = "Гущин С.В.", sum = BigDecimal("717.88"))
        val p13 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700109", fromName = "Липецкий Иван Владимирович", sum = BigDecimal("917.82"))

        val p8194_41 = listOf(p01, p02, p03, p04, p05, p06, p07, p08, p09)
        val p24985_05 = listOf(p10, p11, p12, p13)
        return when (sum) {
            "8195.41" -> makeRegistry(p8194_41)
            "24985.05" -> makeRegistry(p24985_05)
            else -> {
                logger().error("Unknown sum: $sum")
                throw IllegalArgumentException("Unknown sum: $sum")
            }
        }
    }

    fun makeByManualAccount(specialAccount: Boolean) = makeRegistry(
        paymentService.findAllIncomingPaymentsWithFilter(
            pageNum = 0,
            pageSize = MAX_SIZE_PER_PAGE_FOR_EXCEL,
            filter = IncomingPaymentsFilter(type = IncomingPaymentTypeEnum.MANUAL_ACCOUNT)
        ).content
    )

    fun make(specialAccount: Boolean, useInactiveAccount: Boolean) = makeRegistry(ruleService.recognizePayments(specialAccount, useInactiveAccount))

    //Формирование Сбер реестр оплат по лицевым счетам (для типа ACCOUNT и MANUAL_ACCOUNT)
    fun makeRegistry(payments: List<IncomingPayment>): List<String> {
        if (payments.isEmpty()) return emptyList()
        val defaultLocalDateTime = LocalDateTime.of(2020, 11, 12, 10, 10, 10)
        fun ddmmyyyyDateFormat(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        fun hhmmssDateFormat(): DateTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss")
        fun mmyy(): DateTimeFormatter = DateTimeFormatter.ofPattern("MMyy")
        val rows = mutableListOf<RegistryRow>()
        val sum = RegistrySum()
        for (payment in payments) {
            if (payment.type != IncomingPaymentTypeEnum.ACCOUNT && payment.type != IncomingPaymentTypeEnum.MANUAL_ACCOUNT) continue
            rows.add(
                RegistryRow(
                    date = payment.date.format(ddmmyyyyDateFormat()),
                    time = defaultLocalDateTime.format(hhmmssDateFormat()),
                    branchNumber = "5278",
                    cashiersNumber = "5278",
                    eps = "5278",
                    account = payment.account!!,
                    fio = payment.fromName,
                    address = "ПР-Д МАРЬИНОЙ РОЩИ 17-Й МОСКВА Д.1",
                    period = defaultLocalDateTime.format(mmyy()),
                    amount = payment.sum!!.setScale(2, RoundingMode.HALF_UP),
                    transferAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    commission = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    channel = RegistryChannelEnum.ONLINE,
                ).apply { sum.add(this) })
        }
        logger().info("Sum: $sum")

        val lines = rows.map { it.toCSVLine() }.toMutableList()
        if (lines.isEmpty()) return emptyList()
        lines.add(
            RegistryLastLine(
                numberOfLine = rows.size,
                sumAmount = sum.amount.setScale(2, RoundingMode.HALF_UP),
                sumTransferAmount = sum.transferAmount.setScale(2, RoundingMode.HALF_UP),
                sumCommission = sum.commission.setScale(2, RoundingMode.HALF_UP),
                paymentOrderNumber = "558220",
                datePaymentOrder = defaultLocalDateTime.format(ddmmyyyyDateFormat()),
            ).toCSVLine()
        )
        return lines
    }

}