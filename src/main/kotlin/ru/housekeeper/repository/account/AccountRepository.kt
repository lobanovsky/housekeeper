package ru.housekeeper.repository.account

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Account

@Repository
interface AccountRepository : CrudRepository<Account, Long>, AccountRepositoryCustom {

    @Query("select a from Account a where a.special = :special and a.active = true")
    fun findActiveBySpecial(
        special: Boolean,
    ): List<Account>

    @Query("select a from Account a where a.special = :special")
    fun findBySpecial(
        special: Boolean,
    ): List<Account>
}