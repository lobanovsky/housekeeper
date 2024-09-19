package ru.housekeeper.repository.owner

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Owner

@Repository
interface OwnerRepository : CrudRepository<Owner, Long>, OwnerRepositoryCustom {

    @Query("SELECT p FROM Owner p WHERE p.fullName = :fullName")
    fun findByFullName(@Param("fullName") fullName: String): Owner?

}