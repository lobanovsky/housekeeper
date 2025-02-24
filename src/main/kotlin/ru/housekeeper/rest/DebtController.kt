package ru.housekeeper.rest

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.service.DebtService

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