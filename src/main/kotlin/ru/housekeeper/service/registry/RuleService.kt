package ru.housekeeper.service.registry

import org.springframework.stereotype.Service
import ru.housekeeper.enums.payment.IncomingPaymentTypeEnum
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.repository.account.AccountRepository
import ru.housekeeper.repository.payment.IncomingPaymentRepository
import ru.housekeeper.utils.logger
import java.time.LocalDateTime

@Service
class RuleService(
    private val paymentRepository: IncomingPaymentRepository,
    private val accountRepository: AccountRepository,
) {

    //Поиск Лицевого счета в строке назначения платежа и установка его в платеж
    fun recognizePayments(
        specialAccount: Boolean,
        useInactiveAccount: Boolean = false
    ): List<IncomingPayment> {
        val accounts = accountRepository.findBySpecialAndActive(specialAccount, useInactiveAccount)
            .map { it.number }.toSet()
        if (accounts.isEmpty()) {
            logger().warn("Cчета не найдены")
            return emptyList()
        }
        val payments = paymentRepository.findByToAccountsAndTypeIsNull(accounts).filterNot { rules(it) }

        var count = 0
        val updateAccountDateTime = LocalDateTime.now()
        for (payment in payments) {
            val account = accountIdentification(payment, specialAccount)
            if (account == null) {
                payment.type = IncomingPaymentTypeEnum.UNKNOWN_ACCOUNT
                logger().error("Account not found for: UUID: ${payment.uuid}, FromName: ${payment.fromName}, Purpose: ${payment.purpose}")
            } else {
                payment.type = IncomingPaymentTypeEnum.ACCOUNT
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
    fun accountIdentification(payment: IncomingPayment, specialAccount: Boolean): String? {
        //find by rules
        var account = if (specialAccount) findSpecialAccountByRules(payment) else findAccountByRules(payment)
        if (account != null) return account

        //10 digits
        val regex = Regex("\\d{10}")
        val matchResult = regex.find(payment.purpose)
        account = matchResult?.value
        if (account != null) return account

        return null
    }
}