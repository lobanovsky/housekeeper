package ru.housekeeper.repository.access

import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.access.AccessEntity

@Repository
interface AccessRepositoryCustom {

    fun findByPlateNumber(plateNumber: String, active: Boolean = true): List<AccessEntity>

    fun findByAreaId(areaId: Long): List<AccessEntity>

}