package ru.housekeeper.rest

import org.springframework.web.bind.annotation.*
import ru.housekeeper.service.PaymentService
import ru.housekeeper.utils.*

@CrossOrigin
@RestController
@RequestMapping("/counterparties")
class CounterpartyController(
    private val paymentService: PaymentService,
)