package ru.housekeeper.repository.access

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.enums.AccessBlockReasonEnum
import ru.housekeeper.model.entity.access.AccessEntity
import java.time.LocalDateTime

@Repository
interface AccessRepository : CrudRepository<AccessEntity, Long>, AccessRepositoryCustom {

    //find by owner id
    @Query("select a from AccessEntity a where a.ownerId = :ownerId and a.active = :active")
    fun findByOwnerId(
        @Param("ownerId") ownerId: Long,
        @Param("active") active: Boolean = true
    ): List<AccessEntity>

    //find by phone number and owner id
    @Query("select a from AccessEntity a where a.phoneNumber = :number and a.ownerId = :ownerId and a.active = :active")
    fun findByPhoneNumberAndOwnerId(
        @Param("number") number: String,
        @Param("ownerId") ownerId: Long,
        @Param("active") active: Boolean = true
    ): AccessEntity?

    //deactivate by ids
    @Modifying
    @Query("update AccessEntity a set a.active = false, a.blockReason = :reason, a.blockDateTime = :date where a.id in :ids")
    fun deactivateByIds(
        @Param("ids") ids: List<Long>,
        @Param("date") date: LocalDateTime,
        @Param("reason") reason: AccessBlockReasonEnum
    )

    //find all active
    @Query("select a from AccessEntity a where a.active = true")
    fun findAllActive(): List<AccessEntity>
}