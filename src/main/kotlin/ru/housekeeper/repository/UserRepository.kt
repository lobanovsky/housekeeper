package ru.housekeeper.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.User

@Repository
interface UserRepository : CrudRepository<User, Long>, JpaSpecificationExecutor<User> {


    @Query("SELECT u FROM User u WHERE u.email = :email and u.active = true")
    fun findActiveByEmail(email: String): User?

    @Query("""
        from User u
        where u.email = :email
            and array_contains(u.workspaces, :workspaceId)
            and u.active = true
    """)
    fun findByEmailAndWorkspacesContainsAndActiveIsTrue(
        email: String,
        workspaceId: Long,
    ): User?

    @Query("SELECT u FROM User u WHERE u.id = :id and u.active = true")
    fun findActiveById(id: Long): User?

    @Modifying
    @Query("UPDATE User u SET u.active = :active WHERE u.id = :id")
    fun updateActiveById(
        id: Long,
        active: Boolean,
    )

    @Query("SELECT u FROM User u WHERE u.code = :code")
    fun findByCode(
        code: String,
    ): User?

    @Query("""
        from User u
        where u.role != ru.housekeeper.enums.UserRoleEnum.SUPER_ADMIN
            and (:workspaceId is null or array_contains(u.workspaces, :workspaceId))
            and (:email is null or u.email like %:email%)
            and (:name is null or u.name like %:name%)
            and (:active is null or u.active = :active)
    """)
    fun findAllBy(
        workspaceId: Long?,
        email: String?,
        name: String?,
        active: Boolean?,
        pageable: Pageable
    ): Page<User>
}