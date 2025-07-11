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
        assert(getAccount(fromName = "ЛУЦКИЙ АЛЕКСЕЙ АЛЕКСАНДРОВИЧ", purpose = "ЛС 0 0 0 0 5 0 0 1 0 7; Кап. Ремонт. пр-д Марьиной Рощи 17-й дом 1, кв. 107. НДС не облагается") == "0000500107")
        assert(getAccount(fromName = "ЛУЦКИЙ АЛЕКСЕЙ АЛЕКСАНДРОВИЧ", purpose = "ЛС 0 0 0 0 7 0 0 1 3 8; Кап. Ремонт. пр-д Марьиной Рощи 17-й дом 1 (м.м), кв. А/м 138. НДС не облагается") == "0000700138")
        assert(getAccount(fromName = "Бобровский Николай Эдуардович", purpose = "Капитальный ремонт.Машиноместо 35 за март 2025г.;04/04/2025") == "0000700035")
        assert(getAccount(fromName = "Бобровский Николай Эдуардович", purpose = "Капитальный ремонт.Машиноместо 34 за март 2025г.;04/04/2025") == "0000700034")
        assert(getAccount(fromName = "Бобровский Николай Эдуардович", purpose = "Капитальный ремонт 34 за июнь 2025г.;02/07/2025") == "0000700034")
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
        return specialRuleService.accountIdentification(payment, true)
    }
}