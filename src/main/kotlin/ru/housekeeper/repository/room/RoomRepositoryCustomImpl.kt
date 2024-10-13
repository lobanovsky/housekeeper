package ru.housekeeper.repository.room

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.filter.RoomFilter
import ru.housekeeper.repository.equalFilterBy
import ru.housekeeper.repository.likeFilterBy
import ru.housekeeper.utils.getPage

class RoomRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : RoomRepositoryCustom {

    override fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int, filter: RoomFilter
    ): Page<Room> {

        val predicates = mutableMapOf<String, String>()
        predicates["account"] = likeFilterBy("r.account", filter.account)
        predicates["number"] = equalFilterBy("r.number", filter.number)
        predicates["ownerName"] = likeFilterBy("r.ownerName", filter.ownerName)
        predicates["type"] = equalFilterBy("r.type", filter.type)
        predicates["building"] = equalFilterBy("r.buildingId", filter.building)
        val conditions = predicates.values.joinToString(separator = " ")

        val sql = "SELECT r FROM Room r WHERE true = true $conditions ORDER BY r.buildingId, r.account"
        val sqlCount = "SELECT count(r) FROM Room r WHERE true = true $conditions"

        return getPage<Room>(entityManager, sql, sqlCount, pageNum, pageSize)
    }

    override fun findByOwnerId(ownerId: Long): List<Room> {
        val sql = "select * from room where owners @> '[$ownerId]'"
        val query = entityManager.createNativeQuery(sql, Room::class.java)
        return query.resultList as List<Room>
    }

    override fun findByBuildingIdsAndOwnerIds(
        buildingIds: Set<Long>,
        ownerId: Long,
    ): List<Room> {
        val sql = "select * from room where building_id in (:buildingIds) AND owners @> '[$ownerId]'"
        val query = entityManager.createNativeQuery(sql, Room::class.java)
        query.setParameter("buildingIds", buildingIds)
        return query.resultList as List<Room>
    }


}