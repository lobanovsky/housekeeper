package ru.tsn.housekeeper.service.gate

import org.springframework.stereotype.Service
import ru.tsn.housekeeper.model.entity.gate.Gate
import ru.tsn.housekeeper.repository.gate.GateRepository

@Service
class GateService(
    private val gateRepository: GateRepository,
) {

    fun getGateByImei(imei: String): Gate? = gateRepository.findByImei(imei)

}