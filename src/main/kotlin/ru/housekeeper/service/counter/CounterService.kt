package ru.housekeeper.service.counter

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.model.entity.counter.Counter
import ru.housekeeper.parser.counter.WaterCounterParser
import ru.housekeeper.repository.counter.CounterRepository
import ru.housekeeper.rest.FileImporterController
import ru.housekeeper.service.RoomService
import ru.housekeeper.utils.logger

@Service
class CounterService(
    private val counterRepository: CounterRepository,
    private val roomService: RoomService,
) {

    fun upload(file: MultipartFile, checkSum: String, filter: FileImporterController.CounterValue): Int {
        return 0
    }

    fun waterCounterParseAndSave(file: MultipartFile, checkSum: String): Int {
        val counterVOs = WaterCounterParser(file).parse()
        logger().info("Parsed ${counterVOs.size} water counters")
        val roomsByNumber =
            roomService.findByRoomNumbersAndType(counterVOs.map { it.roomNumber }.toSet()).associateBy { it.number }
        val counters = mutableListOf<Counter>()
        counterVOs.forEach {
            counters.add(
                Counter(
                    number = it.counterNumber,
                    counterType = it.counterType,
                    roomId = roomsByNumber[it.roomNumber]?.id,
                    account = roomsByNumber[it.roomNumber]?.account
                )
            )
        }
        val uniqCounters = removeDuplicates(counters)
        logger().info("Try to save ${uniqCounters.size} uniq counters")
        counterRepository.saveAll(uniqCounters)
        return counters.size
    }

    private fun removeDuplicates(counters: List<Counter>): List<Counter> {
        val existed = counterRepository.findAll().map { it.number }.toSet()
        val uploaded = counters.map { it.number }.toSet()
        val duplicates = uploaded intersect existed
        logger().info("Uploaded ${counters.size}, unique -> ${(uploaded subtract existed).size}")
        val groupedCounters = counters.associateBy { it.number }.toMutableMap()
        duplicates.forEach(groupedCounters::remove)
        return groupedCounters.values.toList()
    }

}