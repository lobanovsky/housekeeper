package ru.housekeeper.repository.access

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import ru.housekeeper.model.entity.access.AccessEntity

class AccessRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : AccessRepositoryCustom {

    override fun findByPlateNumber(plateNumber: String, active: Boolean): List<AccessEntity> {
        val sql = "SELECT * FROM access, jsonb_array_elements(cars) AS car WHERE car->>'plateNumber' LIKE '%$plateNumber%' and active=$active"
        val query = entityManager.createNativeQuery(sql, AccessEntity::class.java)
        return query.resultList as List<AccessEntity>
    }

    override fun findByAreaId(areaId: Long): List<AccessEntity> {
        val sql = "SELECT * FROM access, jsonb_array_elements(areas) AS area WHERE area->>'areaId'='$areaId' and active=true"
        val query = entityManager.createNativeQuery(sql, AccessEntity::class.java)
        return query.resultList as List<AccessEntity>
    }
}