package ru.housekeeper.repository.owner

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import ru.housekeeper.model.entity.Owner

class OwnerRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : OwnerRepositoryCustom {

    override fun findByRoomId(roomId: Long, active: Boolean): List<Owner> {
        val sql = "select * from owner where rooms @> '[$roomId]' and active = $active"
        val query = entityManager.createNativeQuery(sql, Owner::class.java)
        return query.resultList as List<Owner>
    }
}