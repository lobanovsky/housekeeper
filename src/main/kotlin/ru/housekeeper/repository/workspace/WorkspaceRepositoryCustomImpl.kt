package ru.housekeeper.repository.workspace

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.Workspace
import ru.housekeeper.repository.likeFilterBy
import ru.housekeeper.utils.getPage

class WorkspaceRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : WorkspaceRepositoryCustom {

    override fun findBy(
        pageNum: Int,
        pageSize: Int,
        name: String?,
    ): Page<Workspace> {
        val predicates = mutableMapOf<String, String>()
        predicates["name"] = likeFilterBy("p.name", name)
        val conditions = predicates.values.joinToString(separator = " ")

        val sql = "SELECT p FROM Workspace p WHERE true = true $conditions ORDER BY p.createDate DESC"
        val sqlCount = "SELECT count(p) FROM Workspace p WHERE true = true $conditions"

        return getPage<Workspace>(entityManager, sql, sqlCount, pageNum, pageSize)
    }

}