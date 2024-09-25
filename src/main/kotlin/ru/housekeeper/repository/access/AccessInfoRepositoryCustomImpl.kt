package ru.housekeeper.repository.access

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import ru.housekeeper.model.entity.access.AccessInfo

class AccessInfoRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : AccessInfoRepositoryCustom {

    override fun findByRoomId(roomId: Long, active: Boolean): List<AccessInfo> {
        val sql = "select * from access_info where rooms @> '[$roomId]' and active = $active"
        val query = entityManager.createNativeQuery(sql, AccessInfo::class.java)
        return query.resultList as List<AccessInfo>
    }

    override fun findByAreaId(areaId: Long, active: Boolean): List<AccessInfo> {
        val sql = "select * from access_info where areas @> '[$areaId]' and active = $active"
        val query = entityManager.createNativeQuery(sql, AccessInfo::class.java)
        return query.resultList as List<AccessInfo>
    }

}