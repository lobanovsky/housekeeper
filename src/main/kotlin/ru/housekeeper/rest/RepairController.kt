package ru.housekeeper.rest

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.service.RepairService
import java.math.BigDecimal
import java.time.LocalDate

@CrossOrigin
@RestController
@RequestMapping("/repairs")
class RepairController(
    private val repairService: RepairService,
) {

    /**
     * Поиск и удаление транзакций (платежей)
     * Поиск одинаковых UUID без учёта времени
     * Например, 2021-01-01 12:00:00 и 2021-01-01 13:00:00 - это одинаковые даты в UUID
     */
    @PutMapping("/remove-duplicates")
    fun removeDuplicates() {
        repairService.findAndRemoveDuplicates()
    }

    /**
     * Обновление UUID для всех платежей
     * Причина: UUID состоит из даты, номера транзакции и суммы
     * Иногда дата может приходить с разным временем, поэтому UUID различается, поэтому исключаем время из даты
     */
    @PutMapping("/update-uuid")
    fun updateUUID() {
        repairService.updateUUID()
    }

    /**
     * Получение суммы входящих платежей, за исключением возвратов депозитов за определенный период
     */
    @PostMapping("/sum-of-payments")
    fun getSumOfPayments(@RequestBody range: RangeRequest): BigDecimal {
        return repairService.getSumOfPayments(range.startDate, range.endDate, range.toAccounts)
    }

    data class RangeRequest(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val toAccounts: List<String>,
    )

    /**
     * Инициализация доступа на территории
     */
    @PostMapping("/init-yard")
    fun initYard(
        @RequestPart file: MultipartFile,
    ): Int = repairService.initYard(file)

    @PostMapping("/init-garage")
    fun initGarage(
        @RequestPart file: MultipartFile,
    ): Int = repairService.initGarage(file)

    /**
     *Блокировка номеров телефонов, которые не пользовались шлагбаумом более n-месяцев
     */
    @GetMapping("/block-expired-phone-numbers")
    fun blockExpiredPhoneNumbers(
        @RequestParam months: Int,
    ) = repairService.blockExpiredPhoneNumbers(months)

    /**
     * Добавление списка зон доступа, которые может указывать собственник
     */
    @GetMapping("/add-availabel-access-areas")
    fun addAreas() = repairService.addAvailablelAccessAreas()
}