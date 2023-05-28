package ru.housekeeper.rest.gate

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.gate.GateService

@CrossOrigin
@RestController
@RequestMapping("/gates")
class GateController(
    private val gateService: GateService
) {

    @Operation(summary = "Get all gates")
    @GetMapping
    fun getAllGates(): List<GateResponse> = gateService.getAllGates().map {
        GateResponse(
            id = it.id,
            name = it.name,
            phoneNumber = it.phoneNumber,
            imei = it.imei,
        )
    }

    data class GateResponse(
        val id: Long,
        val name: String,
        val phoneNumber: String?,
        val imei: String,
    )

}