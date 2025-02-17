package ru.housekeeper.model.response

import ru.housekeeper.model.entity.User
import ru.housekeeper.model.entity.Workspace
import ru.housekeeper.rest.UserController
import ru.housekeeper.utils.color
import java.time.LocalDateTime
import kotlin.collections.map
import kotlin.collections.toSet

data class UserResponse(
    val id: Long?,
    val createDate: LocalDateTime,
    val active: Boolean,
    val email: String,
    val name: String,
    val description: String?,
    var workspaces: Set<AvailableWorkspaceResponse>,
    val role: UserController.RoleResponse,
    val code: String?,
    val ownerId: Long? = null,
)

data class AvailableWorkspaceResponse(
    val id: Long,
    val name: String,
    val color: String
)

fun User.toResponse(workspaces: Collection<Workspace>) = UserResponse(
    id = id,
    createDate = createDate,
    active = active,
    email = email,
    name = name,
    description = description,
    workspaces = workspaces.map { it.toAvailableWorkspaceResponse() }.toSet(),
    role = UserController.RoleResponse(role.name, role.roleName, role.description),
    code = code,
    ownerId = ownerId,
)

fun Workspace.toAvailableWorkspaceResponse() = AvailableWorkspaceResponse(
    id = id,
    name = name,
    color = name.color()
)