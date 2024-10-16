package ru.housekeeper.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.housekeeper.enums.UserRoleEnum
import ru.housekeeper.model.entity.User

data class UserDetailsAdapter(
    val id: Long,
    val workspaces: List<Long>,
    val email: String,
    val role: UserRoleEnum,
    private val password: String,
    private val active: Boolean
) : UserDetails {

    constructor(user: User) : this(
        id = user.id,
        workspaces = user.workspaces,
        email = user.email,
        role = user.role,
        password = user.encodedPassword,
        active = user.active
    )

    override fun getUsername() = email
    override fun getPassword() = password
    override fun isEnabled() = active
    override fun isAccountNonExpired() = true
    override fun isCredentialsNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun getAuthorities() = listOf(SimpleGrantedAuthority(role.name))
}