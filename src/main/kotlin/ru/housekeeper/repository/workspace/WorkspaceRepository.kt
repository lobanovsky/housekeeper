package ru.housekeeper.repository.workspace

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Workspace

@Repository
interface WorkspaceRepository : CrudRepository<Workspace, Long>, WorkspaceRepositoryCustom {

    @Query("SELECT c FROM Workspace c WHERE c.name = :name")
    fun findByName(
        @Param("name") name: String
    ): Workspace?

    @Modifying
    @Query("UPDATE Workspace c SET c.active = :active WHERE c.id = :id")
    fun updateActiveById(
        @Param("id") id: Long,
        @Param("active") active: Boolean
    )

    @Query("SELECT c FROM Workspace c WHERE c.name = :name and c.active = true")
    fun findByNameAndActive(
        @Param("name") name: String
    ): Workspace?


    fun findByActiveAndIdIn(active: Boolean, ids: Collection<Long>): Set<Workspace>
}
