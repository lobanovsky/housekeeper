package ru.housekeeper.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Account

@Repository
interface AccountRepository : CrudRepository<Account, Long> {

    @Query("select a from Account a where a.special = :special and a.active = :active")
    fun findActiveAndSpecial(
        special: Boolean,
        active: Boolean = true,
    ): List<Account>
}