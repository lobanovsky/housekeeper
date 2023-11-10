package ru.housekeeper.service.registry

import org.springframework.stereotype.Service
import ru.housekeeper.enums.IncomingPaymentTypeEnum
import ru.housekeeper.enums.registry.RegistryChannelEnum
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.entity.registry.RegistryLastLine
import ru.housekeeper.model.entity.registry.RegistryRow
import ru.housekeeper.model.entity.registry.RegistrySum
import ru.housekeeper.repository.AccountRepository
import ru.housekeeper.repository.payment.IncomingPaymentRepository
import ru.housekeeper.utils.getFlatAccount
import ru.housekeeper.utils.getParkingAccount
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class AccountRegistryService(
    private val paymentRepository: IncomingPaymentRepository,
    private val accountRepository: AccountRepository,
) {

    fun make(): List<String> = makeRegistry(findAccountsForPayments())

    //Поиск Лицевого счета в строке назначения платежа и установка его в платеж
    fun findAccountsForPayments(): List<IncomingPayment> {
        val accounts = accountRepository.findBySpecial(false).map { it.number }.toSet()
        if (accounts.isEmpty()) {
            logger().warn("Cчета не найдены")
            return emptyList()
        }
        val payments = paymentRepository.findByToAccountsAndAccountIsNull(accounts)
            .filterNot { skipByRules(it) }

        var count = 0
        val updateAccountDateTime = LocalDateTime.now()
        for (payment in payments) {
            val account = findAccountNumberInString(payment)
            if (account == null) {
                payment.type = IncomingPaymentTypeEnum.NOT_DETERMINATE
                logger().error("Account not found for: UUID: ${payment.uuid}, FromName: ${payment.fromName}, Purpose: ${payment.purpose}")
            } else {
                payment.type = IncomingPaymentTypeEnum.DETERMINATE_ACCOUNT
                count++
            }
            //set account and updateAccountDateTime
            payment.account = account
            payment.updateAccountDateTime = updateAccountDateTime
        }
        logger().info("Payments for matcher account: ${payments.size}, found: $count")
        return paymentRepository.saveAll(payments).toList()
    }


    //Поиск Лицевого счета в строке назначения платежа
    fun findAccountNumberInString(payment: IncomingPayment): String? {
        val purpose = payment.purpose

        //find by rules
        var account = findByRules(payment)
        if (account != null) return account

        //10 digits
        val regex = Regex("\\d{10}")
        val matchResult = regex.find(purpose)
        account = matchResult?.value
        if (account != null) return account

        return null
    }

    //Поиск Лицевого счета в строке назначения платежа по правилам
    private fun findByRules(payment: IncomingPayment): String? {

        if (payment.fromName.contains("ЕПИФАНОВА НАДЕЖДА ЕВГЕНЬЕВНА", true))
            return getFlatAccount(4)

        if (payment.fromName.contains("Оболёшев Сергей Леонидович", true)
            && payment.purpose.contains("ММ", true)
        ) return getParkingAccount(42)

        if (payment.fromName.contains("ВИНОГРАДОВ ГЕННАДИЙ АНДРЕЕВИЧ", true)
            && payment.purpose.contains("КВАРПЛАТА, М/М 79", true)
        ) return getParkingAccount(79)

        if (payment.fromName.contains("ВИНОГРАДОВ ГЕННАДИЙ АНДРЕЕВИЧ", true)
            && payment.purpose.contains("КВАРПЛАТА, КВ 68", true)
        ) return getFlatAccount(68)

        if (payment.fromName.contains("МУХТАРОВА НАТАЛЬЯ ТОФИКОВНА", true)
            && payment.purpose.contains("КВАРПЛАТА, НДС НЕ ОБЛАГАЕТСЯ", true)
        ) return getFlatAccount(76)


        return null
    }


    //Пропуск платежей по правилам
    private fun skipByRules(payment: IncomingPayment): Boolean {
        //Сбер реестры
        if (ruleContains(payment, "EPS")) return true
        //Возврат депозита, выплата %% по депозиту
        if (ruleContains(payment, "ПУ00")) return true
        //ВТБ реестры
        if (ruleContains(payment, "_VTB_")) return true
        return false
    }

    private fun ruleContains(payment: IncomingPayment, other: String): Boolean {
        if (payment.purpose.contains(other, ignoreCase = true)) {
            logger().info("Skip by rule: [${payment.id}], date[${payment.date}]: ${payment.purpose}")
            payment.type = IncomingPaymentTypeEnum.SKIP
            return true
        }
        return false
    }


    //Формирование Сбер реестр оплат
    private fun makeRegistry(payments: List<IncomingPayment>): List<String> {
        if (payments.isEmpty()) return emptyList()
        val defaultLocalDateTime = LocalDateTime.of(2020, 11, 12, 10, 10, 10)
        fun ddmmyyyyDateFormat(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        fun hhmmssDateFormat(): DateTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss")
        fun mmyy(): DateTimeFormatter = DateTimeFormatter.ofPattern("MMyy")
        val rows = mutableListOf<RegistryRow>()
        val sum = RegistrySum()
        for (payment in payments) {
            if (payment.type == IncomingPaymentTypeEnum.NOT_DETERMINATE) continue
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