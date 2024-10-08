package ru.housekeeper.utils

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.support.PageableExecutionUtils
import ru.housekeeper.model.dto.RoomVO
import ru.housekeeper.model.entity.Room

fun Room.toRoomVO(): RoomVO {
    return RoomVO(
        id = id,
        street = street,
        building = buildingId,
        cadastreNumber = cadastreNumber,
        account = account,
        ownerName = ownerName,
        number = number,
        certificate = certificate,
        square = square,
        percentage = percentage,
        type = type,
        ownerIds = owners.toList()
    )
}

fun Page<Room>.toRoomVO(pageNum: Int, pageSize: Int): Page<RoomVO> =
    PageableExecutionUtils.getPage(this.content.map { it.toRoomVO() }, PageRequest.of(pageNum, pageSize)) { this.totalElements }
