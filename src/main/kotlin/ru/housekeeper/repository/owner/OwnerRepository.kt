package ru.housekeeper.repository.owner

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.OwnerEntity

@Repository
interface OwnerRepository : CrudRepository<OwnerEntity, Long>, OwnerRepositoryCustom {

    @Query("SELECT p FROM OwnerEntity p WHERE p.fullName = :fullName")
    fun findByFullName(@Param("fullName") fullName: String): OwnerEntity?

}