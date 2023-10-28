package ru.housekeeper.service.registry

import org.springframework.stereotype.Service
import ru.housekeeper.enums.registry.RegistryChannelEnum
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.model.entity.registry.RegistryLastLine
import ru.housekeeper.model.entity.registry.RegistryRow
import ru.housekeeper.model.entity.registry.RegistrySum
import ru.housekeeper.repository.payment.IncomingPaymentRepository
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.removeSpaces
import ru.housekeeper.utils.yyyyMMddHHmmssDateFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class RegistryService(
    private val paymentRepository: IncomingPaymentRepository,
) {

    fun make(bankAccount: String): List<String> = makeRegistry(findAccountsForPayments(bankAccount))

    //Поиск Лицевого счета в строке назначения платежа и установка его в платеж
    private fun findAccountsForPayments(bankAccount: String): List<IncomingPayment> {
        val payments = paymentRepository.findByToAccountAndAccountIsNull(toAccount = bankAccount)
            .filterNot { skipByRules(it.purpose) }
        var count = 0
        val updateAccountDateTime = LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())
        val wrongAccounts = mutableListOf<String>()
        for (payment in payments) {
            val account = processAccount(findAccountNumberInString(payment))
            if (account == null) {
                logger().error("Account not found for: UUID: ${payment.uuid}, FromName: ${payment.fromName}, Purpose: ${payment.purpose}")
            } else {
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
    private fun findAccountNumberInString(payment: IncomingPayment): String? {
        val purpose = payment.purpose.removeSpaces().lowercase()

        //ЛС:0000500111
        var regex = Regex("""лс:\d{10}""")
        var matchResult = regex.find(purpose)
        var account = matchResult?.value?.substring(3)
        if (account != null) return account

        //лси0000500111
        regex = Regex("""лси(\d+)""")
        matchResult = regex.find(purpose)
        account = matchResult?.value?.substring(3)
        if (account != null) return account

        //л/с 0000500111
        regex = Regex("""л/с(\d+)""")
        matchResult = regex.find(purpose)
        account = matchResult?.value?.substring(3)
        if (account != null) return account

        //find by rules
        account = findByRules(payment)
        if (account != null) return account

        return null
    }

    //Поиск Лицевого счета в строке назначения платежа по правилам
    private fun findByRules(payment: IncomingPayment): String? {
        if (payment.fromName.contains("Михайлова Елена Владимировна")) return "0000500017"
        if (payment.fromName.contains("Бобровский Николай Эдуардович") && payment.purpose.contains("Квартира №30")) return "0000500030"
        if (payment.fromName.contains("Казадаев Дмитрий Викторович") && payment.purpose.contains("кв.103")) return "0000500103"
        return null
    }

    //Пропуск платежей по правилам
    private fun skipByRules(purpose: String): Boolean {
        if (purpose.contains("Доход от размещения на депозитном счете")) return true
        if (purpose.contains("Пени по взносам на капремонт по жилпом в МКД")) return true
        if (purpose.contains("Средства бюджета на возм выпадающих доход от предост льгот")) return true
        if (purpose.contains("Взносы на капремонт по")) return true
        if (purpose.contains("Уплачены проценты за период")) return true
        return false
    }

    // 0000 - 4 нуля в начале, потому что иногда бывает меньше или больше
    private fun processAccount(input: String?): String? {
        if (input == null) return null
        val firstIndexOf5 = input.indexOf('5')
        if (firstIndexOf5 == -1) {
            // Если в строке нет цифры 5, возвращаем исходную строку
            return input
        }
        val withoutLeadingZeros = input.substring(firstIndexOf5)
        return "0000$withoutLeadingZeros"
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