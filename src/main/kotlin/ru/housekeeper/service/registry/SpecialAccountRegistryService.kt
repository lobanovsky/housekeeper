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
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class SpecialAccountRegistryService(
    private val paymentRepository: IncomingPaymentRepository,
    private val accountRepository: AccountRepository,
) {

    fun make(): List<String> = makeRegistry(findAccountsForPayments())

    //Поиск Лицевого счета в строке назначения платежа и установка его в платеж
    fun findAccountsForPayments(): List<IncomingPayment> {
        val accounts = accountRepository.findBySpecial(true).map { it.number }.toSet()
        if (accounts.isEmpty()) {
            logger().warn("Специальные счета не найдены")
            return emptyList()
        }
        if (accounts.size > 1) {
            logger().warn("Найдено более одного специального счёта")
            return emptyList()
        }
        val specialBankAccount = accounts.first()

        val payments = paymentRepository.findByToAccountAndAccountIsNull(toAccount = specialBankAccount)
            .filterNot { nonSpecialAccountRules(it) }
        var count = 0
        val updateAccountDateTime = LocalDateTime.now()
        val wrongAccounts = mutableListOf<String>()
        for (payment in payments) {
            val account = findSpecialAccount(payment)
            if (account == null) {
                payment.type = IncomingPaymentTypeEnum.UNKNOWN_ACCOUNT
                logger().error("Account not found for: UUID: ${payment.uuid}, FromName: ${payment.fromName}, Purpose: ${payment.purpose}")
            } else {
                payment.type = IncomingPaymentTypeEnum.ACCOUNT
                count++
            }
            validateAccountLength(account, wrongAccounts)
            //set account and updateAccountDateTime
            payment.account = account
            payment.updateAccountDateTime = updateAccountDateTime
        }
        logger().info("Payments for matcher account: ${payments.size}, found: $count")
        return paymentRepository.saveAll(payments).toList()
    }

    private fun validateAccountLength(account: String?, wrongAccounts: MutableList<String>) {
        if (account != null && account.length != 10) {
            wrongAccounts.add(account)
            logger().error("Wrong account: $account")
        }
    }

    //Поиск Лицевого счета в строке назначения платежа
    fun findSpecialAccount(payment: IncomingPayment): String? {
        //find by rules
        var account = findSpecialAccountByRules(payment)
        if (account != null) return account

        //10 digits
        val regex = Regex("\\d{10}")
        val matchResult = regex.find(payment.purpose)
        account = matchResult?.value
        if (account != null) return account

        return null
    }

//    // 0000 - 4 нуля в начале, потому что иногда бывает меньше или больше
//    fun processAccount(input: String?): String? {
//        if (input == null) return null
//        val firstIndexOf5 = input.indexOf('5')
//        if (firstIndexOf5 == -1) {
//            // Если в строке нет цифры 5, возвращаем исходную строку
//            return null
//        }
//        val withoutLeadingZeros = input.substring(firstIndexOf5)
//        if (withoutLeadingZeros.length != 6) return null
//        return "0000$withoutLeadingZeros"
//    }



    private fun ruleContains(payment: IncomingPayment, other: String): Boolean {
        if (payment.purpose.contains(other, ignoreCase = true)) {
            logger().info("Skip by rule: [${payment.id}], date[${payment.date}]: ${payment.purpose}")
            payment.type = IncomingPaymentTypeEnum.UNKNOWN
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
            if (payment.type == IncomingPaymentTypeEnum.UNKNOWN_ACCOUNT) continue
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