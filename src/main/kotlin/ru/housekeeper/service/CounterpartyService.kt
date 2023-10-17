package ru.housekeeper.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.dto.CounterpartyInfo
import ru.housekeeper.model.dto.toCounterparty
import ru.housekeeper.model.entity.Counterparty
import ru.housekeeper.parser.CounterpartyParser
import ru.housekeeper.repository.CounterpartyRepository
import ru.housekeeper.utils.logger

@Service
class CounterpartyService(
    private val counterpartyRepository: CounterpartyRepository,
) {

    data class FileInfo(val size: Int, val numberOfUnique: Int)

    fun parseAndSave(file: MultipartFile): FileInfo {
        val counterpartiesInfo = CounterpartyParser(file).parse()
        logger().info("Parsed ${counterpartiesInfo.size} counterparties")

        val uniqueCounterparties = removeDuplicates(counterpartiesInfo, counterpartyRepository::findAllUUIDs)

        val counterparties = uniqueCounterparties.map {it.toCounterparty()}
        logger().info("Try to save ${counterparties.size} counterparties")
        counterpartyRepository.saveAll(counterparties)
        return FileInfo(counterpartiesInfo.size, uniqueCounterparties.size)
    }

    private fun removeDuplicates(counterparties: List<CounterpartyInfo>, savedUUIDs: () -> List<String>): List<CounterpartyInfo> {
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

    fun getGroupingByName(): Map<String, List<Counterparty>> = counterpartyRepository.findAll().toList().groupBy { it.name }
}