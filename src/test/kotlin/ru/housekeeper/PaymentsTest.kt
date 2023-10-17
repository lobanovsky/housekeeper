package ru.housekeeper

import org.junit.jupiter.api.Test
import ru.housekeeper.utils.getContractNumberFromDepositPurpose
import ru.housekeeper.utils.simplify

class PaymentsTest {

    @Test
    fun getContractNumberOfDeposit() {
        assert("Перечисление средств во вклад (депозит) по договору  123456789.ПУ00 от 08.09.2021 . НДС не облагается.".getContractNumberFromDepositPurpose() == "123456789")
        assert("Перечисление средств во вклад (депозит) по договору 987654321.ПУ00 от 23.11.2021. НДС не облагается.".getContractNumberFromDepositPurpose() == "987654321")
        assert("Перечисление средств во вклад (депозит) по договору 1122334455.ПУ00 от 22.03.2022, без НДС".getContractNumberFromDepositPurpose() == "1122334455")
    }

    @Test
    fun simplifyCounterpartyName() {
        val originalName = "ООО \"Рога и    копыта\""
        val name = originalName.simplify()
        assert(name == "ооо рога и копыта")
    }
}