package ru.housekeeper.rest.counter

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.counter.CounterService
import ru.housekeeper.utils.logger
import java.nio.file.Files
import java.nio.file.Paths

@CrossOrigin
@RestController
@RequestMapping("/counters")
class CounterController(
    private val counterService: CounterService,
) {

    @GetMapping("/electro/init")
    @Operation(summary = "Инициализация индивидуальных счетчиков электроэнергии")
    fun initElectricityCounters() {
        //read txt file and init ElectricityCounter
        val counters = parser()
        logger().info("Parsed ${counters.size} electricity counters")
        counterService.initElectricityCounters(counters)

    }

    data class ElectricityCounter(
        val roomNumber: String,
        val counterNumber: String,
    )

    private fun parser(): List<ElectricityCounter> {
        // Путь к файлу
        val filePath = "/Users/evgeny/Projects/tsn/housekeeper/etc/counter/e.txt"

        // Читаем файл и парсим строки в список объектов ElectricityCounter
        val counters = Files.readAllLines(Paths.get(filePath)).mapNotNull { line ->
            val parts = line.split("\t") // Разделяем строку по табуляции
            if (parts.size == 2) {
                ElectricityCounter(parts[0], parts[1])
            } else {
                null // Игнорируем строки с некорректным форматом
            }
        }
        return counters
    }

}