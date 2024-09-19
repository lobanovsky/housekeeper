package ru.housekeeper.repository.access

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.access.AccessInfo

@Repository
interface AccessInfoRepository : CrudRepository<AccessInfo, Long>, AccessInfoRepositoryCustom {

    @Query("select p from AccessInfo p where p.phoneNumber = :number and p.active = :active")
    fun findByPhoneNumber(
        @Param("number") number: String,
        @Param("active") active: Boolean = true
    ): AccessInfo?

    @Query("select p from AccessInfo p where p.ownerId = :ownerId and p.active = :active")
    fun findByOwnerId(
        ownerId: Long,
        active: Boolean = true
    ): List<AccessInfo>

}