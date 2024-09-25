package ru.housekeeper.repository.access

import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.access.AccessInfo

@Repository
interface AccessInfoRepositoryCustom {

    fun findByRoomId(roomId: Long, active: Boolean = true): List<AccessInfo>

    fun findByAreaId(areaId: Long, active: Boolean = true): List<AccessInfo>

}