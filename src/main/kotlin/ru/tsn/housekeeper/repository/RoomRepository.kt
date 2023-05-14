package ru.tsn.housekeeper.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.tsn.housekeeper.model.entity.Room
import ru.tsn.enums.RoomTypeEnum
import java.math.BigDecimal

@Repository
interface RoomRepository : CrudRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.number = :number AND r.type = :type")
    fun findByNumberAndType(
        @Param("number") number: String?,
        @Param("type") type: RoomTypeEnum = RoomTypeEnum.FLAT
    ): Room?

    @Query("SELECT r FROM Room r WHERE LOWER(r.ownerName) = :ownerName AND r.square = :square AND (r.type = 'GARAGE' OR r.type = 'OFFICE')")
    fun findGarageOrOfficeByOwnerNameAndSquare(
        @Param("ownerName") ownerName: String,
        @Param("square") square: BigDecimal
    ): List<Room>

    @Query("select r from Room r where r.number in (:roomNumbers) AND r.type = :type")
    fun findByRoomNumbersAndType(
        @Param("roomNumbers") roomNumbers: Set<String>,
        @Param("type") type: RoomTypeEnum = RoomTypeEnum.FLAT
    ): List<Room>
}