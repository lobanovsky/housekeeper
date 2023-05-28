package ru.housekeeper.service.gate

import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.gate.Gate
import ru.housekeeper.repository.gate.GateRepository

@Service
class GateService(
    private val gateRepository: GateRepository,
) {

    fun getGateByImei(imei: String): Gate? = gateRepository.findByImei(imei)

    fun getAllGates(): List<Gate> = gateRepository.findAll().toList()

}