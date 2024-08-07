package ru.housekeeper.repository.access

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.access.AccessInfo

@Repository
interface AccessPhoneRepository : CrudRepository<AccessInfo, Long>, AccessPhoneRepositoryCustom {

    @Query("select p from AccessInfo p where p.phoneNumber = :number and p.active = :active")
    fun findByPhoneNumber(
        @Param("number") number: String,
        @Param("active") active: Boolean = true
    ): AccessInfo?

}