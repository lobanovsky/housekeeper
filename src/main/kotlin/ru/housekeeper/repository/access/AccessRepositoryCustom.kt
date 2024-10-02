package ru.housekeeper.repository.access

import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.access.Access

@Repository
interface AccessRepositoryCustom {

    fun findByRoomId(roomId: Long, active: Boolean = true): List<Access>

    fun findByAreaId(areaId: Long, active: Boolean = true): List<Access>

}