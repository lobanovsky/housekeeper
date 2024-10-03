package ru.housekeeper.repository.access

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import ru.housekeeper.model.entity.access.Access

class AccessRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : AccessRepositoryCustom {

    override fun findByRoomId(roomId: Long, active: Boolean): List<Access> {
        val sql = "select * from access where rooms @> '[$roomId]' and active = $active"
        val query = entityManager.createNativeQuery(sql, Access::class.java)
        return query.resultList as List<Access>
    }

    override fun findByAreaId(areaId: Long, active: Boolean): List<Access> {
        val sql = "select * from access where areas @> '[$areaId]' and active = $active"
        val query = entityManager.createNativeQuery(sql, Access::class.java)
        return query.resultList as List<Access>
    }

    override fun findByOwnerId(ownerId: Long, active: Boolean): List<Access> {
        val sql = "select * from access where owners @> '[$ownerId]' and active = $active"
        val query = entityManager.createNativeQuery(sql, Access::class.java)
        return query.resultList as List<Access>
    }
}