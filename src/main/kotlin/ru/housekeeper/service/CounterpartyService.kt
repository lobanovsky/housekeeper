package ru.housekeeper.service

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.model.entity.IncomingPayment
import ru.housekeeper.model.filter.CompanyPaymentsFilter
import ru.housekeeper.repository.CounterpartyRepository
import ru.housekeeper.utils.entityNotfound
import ru.housekeeper.utils.logger

@Service
class CounterpartyService(
    private val counterpartyRepository: CounterpartyRepository,
    private val paymentService: PaymentService,
) {

    data class FileInfo(val size: Int, val numberOfUnique: Int)

    fun parseAndSave(file: MultipartFile): FileInfo {
        val counterpartiesInfo = parser(file)
        logger().info("Parsed ${counterpartiesInfo.size} counterparties")

        val counterparties = mutableListOf<Counterparty>()
        counterpartiesInfo.forEach {
            counterparties.add(
                Counterparty(
                    name = it.name,
                    inn = it.inn,
                    bank = it.bank,
                    bik = it.bik,
                    sign = it.sign
                )
            )
        }
        logger().info("Try to save ${counterparties.size} counterparties")
        counterpartyRepository.saveAll(counterparties)
        return FileInfo(counterparties.size, counterparties.filter { it.inn.isNotEmpty() }.map { it.inn }.toSet().size)
    }

    private fun parser(file: MultipartFile): List<CounterpartyInfo> {
        logger().info("Start parsing counterparties file")
        val workbook = WorkbookFactory.create(file.inputStream)
        val sheet = workbook.getSheetAt(0)

        val nameNum = 0
        val innNum = 1
        val bankNum = 2
        val bikNum = 3
        val signNum = 4

        logger().info("Reading ${sheet.lastRowNum} rows from sheet: ${sheet.sheetName}")

        val counterpartyInfos = mutableListOf<CounterpartyInfo>()
        for (rowNum in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowNum) ?: continue

            val name = row.getCell(nameNum)?.toString() ?: ""
            val inn = row.getCell(innNum)?.toString() ?: ""
            val bank = row.getCell(bankNum)?.toString() ?: ""
            val bik = row.getCell(bikNum)?.toString() ?: ""
            val sign = row.getCell(signNum)?.toString() ?: ""
            logger().info("$rowNum) $name $inn $bank $bik $sign")
            if (inn.isBlank()) {
                logger().info("Inn for $name is blank")
            }
            counterpartyInfos.add(
                CounterpartyInfo(
                    name = name,
                    inn = inn,
                    bank = bank,
                    bik = bik,
                    sign = sign,
                )
            )
        }
        return counterpartyInfos
    }

    data class CounterpartyInfo(
        val name: String,
        val inn: String,
        val bank: String,
        val bik: String,
        val sign: String,
    )

    fun findById(id: Long): Counterparty = counterpartyRepository.findByIdOrNull(id) ?: entityNotfound("Counterparty" to id)

    fun findAllFromCompanyByFilter(inn: String, pageNum: Int, pageSize: Int, filter: CompanyPaymentsFilter): Page<IncomingPayment> =
        paymentService.findAllFromCompanyByFilter(inn, pageNum, pageSize, filter)

}