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
import ru.housekeeper.utils.getSpecialAccount
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.removeSpaces
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
            .filterNot { skipByRules(it) }
        var count = 0
        val updateAccountDateTime = LocalDateTime.now()
        val wrongAccounts = mutableListOf<String>()
        for (payment in payments) {
            val account = processAccount(findAccountNumberInString(payment))
            if (account == null) {
                payment.type = IncomingPaymentTypeEnum.NOT_DETERMINATE
                logger().error("Account not found for: UUID: ${payment.uuid}, FromName: ${payment.fromName}, Purpose: ${payment.purpose}")
            } else {
                payment.type = IncomingPaymentTypeEnum.DETERMINATE_ACCOUNT
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
    fun findAccountNumberInString(payment: IncomingPayment): String? {
        val purpose = payment.purpose.removeSpaces().lowercase()

        //find by rules
        var account = findByRules(payment)
        if (account != null) return account

        //ЛС:0000500111
        var regex = Regex("""лс:\d{10}""")
        var matchResult = regex.find(purpose)
        account = matchResult?.value?.substring(3)
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

        //л/счет 0000500048
        regex = Regex("""л/счет(\d+)""")
        matchResult = regex.find(purpose)
        account = matchResult?.value?.substring(3)
        if (account != null) return account

        //лс0000500021
        regex = Regex("""лс(\d+)""")
        matchResult = regex.find(purpose)
        account = matchResult?.value?.substring(3)
        if (account != null) return account

        return null
    }

    //Поиск Лицевого счета в строке назначения платежа по правилам
    private fun findByRules(payment: IncomingPayment): String? {
        if (payment.fromName.contains("Михайлова Елена Владимировна", true)) return getSpecialAccount(17)

        if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
            && payment.purpose.contains("Квартира №30", true)
        ) return getSpecialAccount(30)

        if (payment.fromName.contains("Казадаев Дмитрий Викторович", true)
            && payment.purpose.contains("кв.103", true)
        ) return getSpecialAccount(103)

        if (payment.fromName.contains("Таланова Наталья Алексеевна", true)
            && payment.purpose.contains("Кап.Ремонт", true)
        ) return getSpecialAccount(11)

        if (payment.fromName.contains("КОПЫЛОВА СВЕТЛАНА ГЕННАДЬ", true)
            && payment.purpose.contains("КВ.104", true)
        ) return getSpecialAccount(104)

        return null
    }

    // 0000 - 4 нуля в начале, потому что иногда бывает меньше или больше
    fun processAccount(input: String?): String? {
        if (input == null) return null
        val firstIndexOf5 = input.indexOf('5')
        if (firstIndexOf5 == -1) {
            // Если в строке нет цифры 5, возвращаем исходную строку
            return null
        }
        val withoutLeadingZeros = input.substring(firstIndexOf5)
        if (withoutLeadingZeros.length != 6) return null
        return "0000$withoutLeadingZeros"
    }

    //Пропуск платежей по правилам
    private fun skipByRules(payment: IncomingPayment): Boolean {
        if (ruleContains(payment, "Доход от размещения на депозитном счете")) return true
        if (ruleContains(payment, "Пени по взносам на капремонт по жилпом в МКД")) return true
        if (ruleContains(payment, "Средства бюджета на возм выпадающих доход от предост льгот")) return true
        if (ruleContains(payment, "Взносы на капремонт по")) return true
        if (ruleContains(payment, "Уплачены проценты за период")) return true
        if (ruleContains(payment, "Взносы капремонт жилпом в МКД адрес Марьиной рощи 17-й пр. д.1 за период")) return true
        if (ruleContains(payment, "Взносы капремонт нежилпом в МКД адрес Марьиной рощи 17-й пр. д.1 за период")) return true

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