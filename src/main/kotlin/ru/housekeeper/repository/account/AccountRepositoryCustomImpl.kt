package ru.housekeeper.repository.account

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import ru.housekeeper.model.entity.Account
import ru.housekeeper.repository.equalFilterBy

class AccountRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : AccountRepositoryCustom {

    override fun findBySpecialAndActive(
        special: Boolean,
        active: Boolean
    ): List<Account> {
        val predicates = mutableMapOf<String, String>()
        predicates["special"] = equalFilterBy("a.special", special)
        predicates["active"] = equalFilterBy("a.number", active)
        val conditions = predicates.values.joinToString(separator = " ")
        val sql = "select a from Account a where true = true $conditions"
        val query = entityManager.createQuery(sql, Account::class.java)
        return query.resultList
    }
}