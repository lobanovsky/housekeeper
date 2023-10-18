package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.CounterpartyInfo
import ru.housekeeper.model.dto.counterparty.CounterpartyRequest
import ru.housekeeper.model.dto.counterparty.CounterpartyResponse
import ru.housekeeper.model.dto.counterparty.toResponse
import ru.housekeeper.model.dto.toCounterparty
import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.parser.CounterpartyParser
import ru.housekeeper.repository.CounterpartyRepository
import ru.housekeeper.utils.entityNotfound
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.simplify

@Service
class CounterpartyService(
    private val counterpartyRepository: CounterpartyRepository,
) {

    data class FileInfo(val size: Int, val numberOfUnique: Int)

    @Transactional
    fun parseAndSave(file: MultipartFile): FileInfo {
        val counterpartiesInfo = CounterpartyParser(file).parse()
        logger().info("Parsed ${counterpartiesInfo.size} counterparties")
        val uniqueCounterparties = removeDuplicates(counterpartiesInfo, counterpartyRepository::findAllUUIDs)
        val savedCounterparties = save(uniqueCounterparties.map { it.toCounterparty() })
        return FileInfo(counterpartiesInfo.size, savedCounterparties.size)
    }

    fun save(counterparties: List<Counterparty>): List<CounterpartyResponse> =
        counterpartyRepository.saveAll(counterparties)
            .also { logger().info("Saved ${counterparties.size} counterparties") }
            .toList()
            .map { it.toResponse() }

    private fun removeDuplicates(
        counterparties: List<CounterpartyInfo>,
        savedUUIDs: () -> List<String>
    ): List<CounterpartyInfo> {
        val saved = savedUUIDs().toSet()
        val uploaded = counterparties.map { it.uuid }.toSet()
        val duplicates = uploaded intersect saved
        logger().info("Uploaded ${uploaded.size}, unique -> ${(uploaded subtract saved).size}")
        val groupedCounterparty = counterparties.associateBy { it.uuid }.toMutableMap()
        duplicates.forEach(groupedCounterparty::remove)
        //show unique
        groupedCounterparty.values.forEach { logger().info("Unique: $it") }
        return groupedCounterparty.values.toList()
    }

    fun findAll(): List<Counterparty> = counterpartyRepository.findAll().toList()

    fun update(counterpartyId: Long, counterpartyRequest: CounterpartyRequest): Counterparty {
        val existCounterparty =
            counterpartyRepository.findByIdOrNull(counterpartyId) ?: entityNotfound("Контрагент" to counterpartyId)
        existCounterparty.uuid = "${counterpartyRequest.originalName.simplify()} $counterpartyRequest.inn"
        existCounterparty.originalName = counterpartyRequest.originalName
        existCounterparty.name = counterpartyRequest.originalName.simplify()
        existCounterparty.inn = counterpartyRequest.inn
        existCounterparty.bank = counterpartyRequest.bank
        existCounterparty.bik = counterpartyRequest.bik
        return counterpartyRepository.save(existCounterparty)
    }

}