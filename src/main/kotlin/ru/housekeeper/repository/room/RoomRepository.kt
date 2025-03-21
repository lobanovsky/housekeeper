package ru.housekeeper.repository.room

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.entity.Room
import java.math.BigDecimal

@Repository
interface RoomRepository : CrudRepository<Room, Long>, RoomRepositoryCustom {

    @Query("SELECT r FROM Room r WHERE r.number = :number AND r.buildingId = :buildingId AND r.type = :type")
    fun findByNumberAndBuildingIdAndType(
        number: String?,
        buildingId: Long,
        type: RoomTypeEnum = RoomTypeEnum.FLAT
    ): Room?

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

    @Query("select r from Room r where r.id in (:ids)")
    fun findByIds(@Param("ids") ids: Set<Long>): List<Room>
}