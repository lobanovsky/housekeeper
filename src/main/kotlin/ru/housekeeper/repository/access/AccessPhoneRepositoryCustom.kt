package ru.housekeeper.repository.access

import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.access.AccessInfo

@Repository
interface AccessPhoneRepositoryCustom {

    fun findByRoomId(roomId: Long, active: Boolean): List<AccessInfo>

}