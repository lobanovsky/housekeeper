package ru.housekeeper

import org.junit.jupiter.api.Test
import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.service.registry.RuleService
import kotlin.test.Ignore

@Ignore
//@TestPropertySource(locations = ["classpath:application-test.yml"])
//@SpringBootTest
class AccountTest {

    //    @Autowired
    private lateinit var specialRuleService: RuleService

    @Test
    fun findAccountFromPurpose1() {
        assert(getAccount("КАП.РЕМОНТ ПО ЛС:0000500075;03/11/2023") == "0000500075")
    }

    @Test
    fun findAccountFromPurpose2() {
        assert(getAccount("ЛСИ0000001104") == null)
    }

    @Test
    fun findAccountFromPurpose3() {
        assert(getAccount("ЛСИ105,10.2023") == null)
    }

    fun getAccount(purpose: String): String? {
        val payment = IncomingPayment(purpose = purpose)
        return specialRuleService.findAccount(payment, true)
    }
}