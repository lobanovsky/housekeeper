package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.counterparty.*
import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.parser.CounterpartyParser
import ru.housekeeper.repository.CounterpartyRepository
import ru.housekeeper.utils.entityNotfound
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.onlyCyrillicLettersAndNumbers
import ru.housekeeper.utils.onlyLettersAndNumber

@Service
class CounterpartyService(
    private val counterpartyRepository: CounterpartyRepository,
) {

    data class FileInfo(val size: Int, val numberOfUnique: Int)

    @Transactional
    fun parseAndSave(file: MultipartFile): FileInfo {
        val counterparties = CounterpartyParser(file).parse()
        logger().info("Parsed ${counterparties.size} counterparties")

        val uniqueCounterparties = removeDuplicates(counterparties, counterpartyRepository::findAllUUIDs)
        val savedCounterparties = saveNonExistent(uniqueCounterparties.map { it.toCounterparty() })
        return FileInfo(counterparties.size, savedCounterparties.size)
    }

    private fun removeDuplicates(
        counterparties: List<CounterpartyVO>,
        savedUUIDs: () -> List<String>
    ): List<CounterpartyVO> {
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

    private fun saveNonExistent(counterparties: List<Counterparty>): List<CounterpartyResponse> =
        counterpartyRepository.saveAll(counterparties)
            .also { logger().info("Saved ${counterparties.size} counterparties") }
            .toList()
            .map { it.toResponse() }

    fun save(counterparty: Counterparty): CounterpartyResponse {
        counterpartyRepository.findByUUID(counterparty.uuid)?.let {
            throw IllegalArgumentException("Контрагент [${it.name}] с UUID(ИНН/имя) [${counterparty.uuid}] уже существует")
        }
        return counterpartyRepository.save(counterparty)
            .also { logger().info("Saved $counterparty") }
            .toResponse()
    }

    fun findAll(): List<Counterparty> = counterpartyRepository.findAll().toList()

    fun update(counterpartyId: Long, counterpartyRequest: CounterpartyRequest): Counterparty {
        val existCounterparty =
            counterpartyRepository.findByIdOrNull(counterpartyId) ?: entityNotfound("Контрагент" to counterpartyId)
        existCounterparty.uuid = makeUUID(counterpartyRequest.inn, counterpartyRequest.name)
        existCounterparty.name = counterpartyRequest.name
        existCounterparty.inn = counterpartyRequest.inn
        return counterpartyRepository.save(existCounterparty)
    }
}

fun makeUUID(inn: String?, name: String): String =
    inn?.ifBlank { name.onlyCyrillicLettersAndNumbers() } ?: name.onlyLettersAndNumber()