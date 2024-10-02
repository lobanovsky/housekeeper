package ru.housekeeper.repository.access

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.enums.AccessBlockReasonEnum
import ru.housekeeper.model.entity.access.Access
import java.time.LocalDateTime

@Repository
interface AccessRepository : CrudRepository<Access, Long>, AccessRepositoryCustom {

    @Query("select p from Access p where p.phoneNumber like %:number% and p.active = :active")
    fun findByPhoneNumber(
        @Param("number") number: String,
        @Param("active") active: Boolean = true
    ): Access?

    @Query("select p from Access p where p.ownerId = :ownerId and p.active = :active")
    fun findByOwnerId(
        ownerId: Long,
        active: Boolean = true
    ): List<Access>

    //Deactivate all access by id
    @Modifying
    @Query("update Access p set p.active = false, p.blockDateTime = :blockedDateTime, p.blockReason = :blockReason where p.id = :id")
    fun deactivateById(
        id: Long,
        blockedDateTime: LocalDateTime,
        blockReason: AccessBlockReasonEnum
    )
}