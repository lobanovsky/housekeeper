package ru.housekeeper.repository.owner

import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.OwnerEntity

@Repository
interface OwnerRepositoryCustom {

    fun findByRoomId(roomId: Long, active: Boolean): List<OwnerEntity>

}