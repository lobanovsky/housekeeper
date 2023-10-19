package ru.housekeeper.rest

import org.springframework.web.bind.annotation.*
import ru.housekeeper.model.dto.counterparty.CounterpartyRequest
import ru.housekeeper.model.dto.counterparty.CounterpartyResponse
import ru.housekeeper.model.dto.counterparty.toCounterparty
import ru.housekeeper.model.dto.counterparty.toResponse
import ru.housekeeper.service.CounterpartyService

@CrossOrigin
@RestController
@RequestMapping("/counterparties")
class CounterpartyController(
    private val counterpartyService: CounterpartyService,
) {

    @GetMapping
    fun findAll(): List<CounterpartyResponse> = counterpartyService.findAll().map { it.toResponse() }

    @PostMapping
    fun create(
        @RequestBody counterpartyRequest: CounterpartyRequest
    ): CounterpartyResponse = counterpartyService.save(counterpartyRequest.toCounterparty())

    @PutMapping("/{counterpartyId}")
    fun update(
        @PathVariable counterpartyId: Long,
        @RequestBody counterpartyRequest: CounterpartyRequest
    ): CounterpartyResponse = counterpartyService.update(counterpartyId, counterpartyRequest).toResponse()

}