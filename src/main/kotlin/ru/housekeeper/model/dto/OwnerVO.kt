package ru.housekeeper.model.dto

import ru.housekeeper.model.entity.Owner
import ru.housekeeper.model.entity.Room
import ru.housekeeper.utils.toRoomVO
import java.time.LocalDateTime

class OwnerVO(
    val id: Long? = null,
    val fullName: String,
    private val emails: MutableSet<String> = mutableSetOf(),
    private var phones: MutableSet<String> = mutableSetOf(),
    private var active: Boolean = true,
    private var dateOfLeft: LocalDateTime? = null,
    var rooms: List<RoomVO> = mutableListOf(),
) {

    fun toOwner(checksum: String): Owner {
        return Owner(
            fullName = fullName,
            emails = emails,
            phones = phones,
            active = active,
            dateOfLeft = dateOfLeft,
            rooms = mutableSetOf(),
            source = checksum,
        )
    }
}

fun Owner.toOwnerVO(rooms: List<Room>): OwnerVO = OwnerVO(
    id = this.id,
    fullName = this.fullName,
    rooms = rooms.map { it.toRoomVO() },
)
