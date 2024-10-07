package ru.housekeeper.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.AreaEntity

@Repository
interface AreaRepository : CrudRepository<AreaEntity, Long> {
    //get all areas by ids
    @Query("select a from AreaEntity a where a.id in :ids")
    fun findAllByIdIn(ids: Set<Long>): List<AreaEntity>
}