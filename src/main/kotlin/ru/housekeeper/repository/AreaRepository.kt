package ru.housekeeper.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Area

@Repository
interface AreaRepository : CrudRepository<Area, Long> {
    //get all areas by ids
    @Query("select a from Area a where a.id in :ids")
    fun findAllByIdIn(ids: Set<Long>): List<Area>
}