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
        //8195,41 - 499 pack
        val p01 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000004165", fromName = "Безмен Виктор Георгиевич", sum = BigDecimal("4338.61"))
        val p02 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700077", fromName = "Скалозуб Виктория Николаевна", sum = BigDecimal("358.94"))
        val p03 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700135", fromName = "Коковина Ольга Ивановна", sum = BigDecimal("296.31"))
        val p04 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700137", fromName = "Комбаров Александр Анатольевич", sum = BigDecimal("607.06"))
        val p05 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700091", fromName = "Коковина Ольга Ивановна", sum = BigDecimal("301.13"))
        val p06 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700098", fromName = "Романовский Геннадий Георгиевич", sum = BigDecimal("722.70"))
        val p07 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700109", fromName = "Липецкий Иван Владимирович", sum = BigDecimal("917.82"))
        val p08 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700134", fromName = "Коковина Ольга Ивановна", sum = BigDecimal("293.90"))
        val p09 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700075", fromName = "Бессараб И. В.", sum = BigDecimal("358.94"))
        //24985,05 - 500 pack
        val p10 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700129", fromName = "Гогохия Д.Г.", sum = BigDecimal("22631.47"))
        val p11 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700117", fromName = "Гущин С.В.", sum = BigDecimal("717.88"))
        val p12 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700118", fromName = "Гущин С.В.", sum = BigDecimal("717.88"))
        val p13 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700109", fromName = "Липецкий Иван Владимирович", sum = BigDecimal("917.82"))
        //flat 53471,49 - 501 pack
        val p_f_7 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500007", fromName = "0000500007", sum = BigDecimal("2329.50"))
        val p_f_8 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500008", fromName = "0000500008", sum = BigDecimal("922.65"))
        val p_f_13 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500013", fromName = "0000500013", sum = BigDecimal("2336.73"))
        val p_f_17 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500017", fromName = "0000500017", sum = BigDecimal("927.47"))
        val p_f_19 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500019", fromName = "0000500019", sum = BigDecimal("4678.28"))
        val p_f_22 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500022", fromName = "0000500022", sum = BigDecimal("1363.49"))
        val p_f_25 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500025", fromName = "0000500025", sum = BigDecimal("2329.50"))
        val p_f_29 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500029", fromName = "0000500029", sum = BigDecimal("463.74"))
        val p_f_30 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500030", fromName = "0000500030", sum = BigDecimal("1387.58"))
        val p_f_34 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500034", fromName = "0000500034", sum = BigDecimal("1356.27"))
        val p_f_38 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500038", fromName = "0000500038", sum = BigDecimal("927.47"))
        val p_f_40 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500040", fromName = "0000500040", sum = BigDecimal("1363.49"))
        val p_f_46 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500046", fromName = "0000500046", sum = BigDecimal("692.59"))
        val p_f_51 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500051", fromName = "0000500051", sum = BigDecimal("2405.46"))
        val p_f_60 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500060", fromName = "0000500060", sum = BigDecimal("1806.75"))
        val p_f_63 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500063", fromName = "0000500063", sum = BigDecimal("1387.58"))
        val p_f_64 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500064", fromName = "0000500064", sum = BigDecimal("734.75"))
        val p_f_74 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500074", fromName = "0000500074", sum = BigDecimal("944.33"))
        val p_f_78 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500078", fromName = "0000500078", sum = BigDecimal("1404.44"))
        val p_f_85 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500085", fromName = "0000500085", sum = BigDecimal("2365.64"))
        val p_f_88 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500088", fromName = "0000500088", sum = BigDecimal("1385.19"))
        val p_f_91 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500091", fromName = "0000500091", sum = BigDecimal("2372.87"))
        val p_f_93 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500093", fromName = "0000500093", sum = BigDecimal("1385.18"))
        val p_f_94 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500094", fromName = "0000500094", sum = BigDecimal("2327.10"))
        val p_f_98 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500098", fromName = "0000500098", sum = BigDecimal("941.92"))
        val p_f_103 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500103", fromName = "0000500103", sum = BigDecimal("2372.87"))
        val p_f_108 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500108", fromName = "0000500108", sum = BigDecimal("1799.52"))
        val p_f_111 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500111", fromName = "0000500111", sum = BigDecimal("1382.77"))
        val p_f_117 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500117", fromName = "0000500117", sum = BigDecimal("1385.18"))
        val p_f_120 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500120", fromName = "0000500120", sum = BigDecimal("1416.49"))
        val p_f_123 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500123", fromName = "0000500123", sum = BigDecimal("1385.18"))
        val p_f_130 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500130", fromName = "0000500130", sum = BigDecimal("1385.17"))
        val p_f_138 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000500138", fromName = "0000500138", sum = BigDecimal("1804.34"))

        //144209.01 - 689 pack
        val p2025_01 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700095", fromName = "Баранов Юрий Васильевич", sum = BigDecimal("18231.33"))
        val p2025_02 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000004166", fromName = "Беспалов Роман Александрович", sum = BigDecimal("62572.10"))
        val p2025_03 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700128", fromName = "Битерман Оскар Эдуардович", sum = BigDecimal("20177.99"))
        val p2025_04 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700026", fromName = "Канарейкина Наталья Ивановна", sum = BigDecimal("21758.85"))
        val p2025_05 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700017", fromName = "Шапиро Владислав Маркович", sum = BigDecimal("21468.74"))

        //11969.90 - 700 pack
        val p2025_08 = IncomingPayment(type = IncomingPaymentTypeEnum.ACCOUNT, account = "0000700076", fromName = "Цветкова Екатерина Валентиновна", sum = BigDecimal("11969.90"))

        val p8194_41 = listOf(p01, p02, p03, p04, p05, p06, p07, p08, p09)
        val p24985_05 = listOf(p10, p11, p12, p13)
        val p_f53471_49 = listOf(p_f_7, p_f_8, p_f_13, p_f_17, p_f_19, p_f_22, p_f_25, p_f_29, p_f_30, p_f_34, p_f_38, p_f_40, p_f_46, p_f_51, p_f_60, p_f_63, p_f_64, p_f_74, p_f_78, p_f_85, p_f_88, p_f_91, p_f_93, p_f_94, p_f_98, p_f_103, p_f_108, p_f_111, p_f_117, p_f_120, p_f_123, p_f_130, p_f_138)
        val p144209_01 = listOf(p2025_01, p2025_02, p2025_03, p2025_04, p2025_05)
        val p11969_90 = listOf(p2025_08)
        return when (sum) {
            "8195.41" -> makeRegistry(p8194_41)
            "24985.05" -> makeRegistry(p24985_05)
            "53471.49" -> makeRegistry(p_f53471_49)
            "144209.01" -> makeRegistry(p144209_01)
            "11969.90" -> makeRegistry(p11969_90)
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