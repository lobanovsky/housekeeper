package ru.housekeeper.repository.owner

import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Owner

@Repository
interface OwnerRepositoryCustom {

    fun findByRoomId(roomId: Long, active: Boolean): List<Owner>

}