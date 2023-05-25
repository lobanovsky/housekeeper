package ru.housekeeper.repository.room

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.filter.RoomFilter
import ru.housekeeper.repository.filterBy
import ru.housekeeper.repository.getPage

class RoomRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : RoomRepositoryCustom {

    override fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int, filter: RoomFilter
    ): Page<Room> {

        val predicates = mutableMapOf<String, String>()
        predicates["account"] = filterBy("r.account", filter.account)
        predicates["number"] = filterBy("r.number", filter.number)
        predicates["ownerName"] = filterBy("r.ownerName", filter.ownerName)
        predicates["type"] = filterBy("r.type", filter.type)
        val conditions = predicates.values.joinToString(separator = " ")

        val sql = "SELECT r FROM Room r WHERE true = true $conditions ORDER BY r.building, r.account DESC"
        val sqlCount = "SELECT count(r) FROM Room r WHERE true = true $conditions"

        return getPage<Room>(entityManager, sql, sqlCount, pageNum, pageSize)

    }

}