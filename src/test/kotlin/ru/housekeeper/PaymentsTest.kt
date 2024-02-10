package ru.housekeeper

import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import ru.housekeeper.parser.PaymentParser
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

    @Test
    fun bikAndName() {
        val bikAndName = "БИК 044525232 ПАО \"МТС-Банк\", г.Москва"
        val (bik, name) = PaymentParser(MockMultipartFile("fileName", "".byteInputStream())).bikAndNameParserV2(
            bikAndName
        )
        assert(bik == "044525232")
        assert(name == "ПАО \"МТС-Банк\", г.Москва")
    }

    @Test
    fun getAccount() {
        val s = "40817810600050415936Фомина Лариса Владимировна"
        //get first 20 numbers
        val account = s.substring(0, 20)
        val name = s.substring(20)
        assert(account == "40817810600050415936")
        assert(name == "Фомина Лариса Владимировна")
    }
}