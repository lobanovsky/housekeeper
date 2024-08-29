package ru.housekeeper

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.repository.account.AccountRepository
import ru.housekeeper.repository.payment.IncomingPaymentRepository
import ru.housekeeper.service.registry.RuleService

@ExtendWith(MockKExtension::class)
class AccountTest {

    @InjectMockKs
    private lateinit var specialRuleService: RuleService

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var incomingPaymentRepository: IncomingPaymentRepository

    @Test
    fun findAccountFromPurpose1() {
        assert(getAccount(purpose = "КАП.РЕМОНТ ПО ЛС:0000500075;03/11/2023") == "0000500075")
        assert(getAccount(fromName = "БОЕВ РОМАН БОРИСОВИЧ", purpose = "СЧЕТ 700105") == "0000700105")
        assert(getAccount(fromName = "БОЕВ РОМАН БОРИСОВИЧ", purpose = "700105. НДС не облагается") == "0000700105")
    }

    @Test
    fun findAccountFromPurpose2() {
        assert(getAccount(purpose = "ЛСИ0000001104") == "0000001104")
    }

    @Test
    fun findAccountFromPurpose3() {
        assert(getAccount(purpose = "ЛСИ105,10.2023") == null)
    }

    private fun getAccount(fromName: String = "", purpose: String): String? {
        val payment = IncomingPayment(fromName = fromName, purpose = purpose)
        return specialRuleService.findAccount(payment, true)
    }
}