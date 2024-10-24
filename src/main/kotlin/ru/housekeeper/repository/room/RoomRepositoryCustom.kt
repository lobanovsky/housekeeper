package ru.housekeeper.repository.room

import org.springframework.data.domain.Page
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.filter.RoomFilter

@Repository
interface RoomRepositoryCustom {

    fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: RoomFilter
    ): Page<Room>

    fun findByOwnerId(ownerId: Long): List<Room>

    fun findByBuildingIdsAndOwnerIds(
        @Param("buildingIds") buildingIds: Set<Long>,
        @Param("ownerIds") ownerId: Long
    ): List<Room>

}