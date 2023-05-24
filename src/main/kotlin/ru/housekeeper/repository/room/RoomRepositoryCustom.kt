package ru.housekeeper.repository.room

import org.springframework.data.domain.Page
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

}