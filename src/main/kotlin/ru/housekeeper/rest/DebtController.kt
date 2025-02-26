package ru.housekeeper.rest

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.service.DebtService

/**
 * Формирование претензий к должникам по капитальному ремонту
 * 1. Загрузка долгов из Excel в базу данных
 * 2. Генерация претензий на основании долгов из БД + шаблон претензии в формате Word
 */
@CrossOrigin
@RestController
@RequestMapping("/debts")
class DebtController(
    private val debtService: DebtService,
) {

    //Загрузка долгов из Excel
    @PostMapping(value = ["/load-from-excel"])
    fun loadDebts(
        @RequestPart file: MultipartFile,
    ) {
        debtService.parseAndSave(file)
    }

    @GetMapping(value = ["/generator"])
    fun generateDebts() {
        debtService.generateDebts()
    }

}