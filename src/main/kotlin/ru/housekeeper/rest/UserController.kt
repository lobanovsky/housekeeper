package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.UserRoleEnum
import ru.housekeeper.model.request.UserRequest
import ru.housekeeper.service.UserService

@Tag(name = "User")
@CrossOrigin
@RestController
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/users/{userId}")
    fun getUser(
        @PathVariable userId: Long,
    ) = userService.findById(userId)

    @GetMapping("/workspaces/{workspaceId}/users")
    fun getAllUsers(
        @PathVariable workspaceId: Long,
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @RequestParam(value = "email", required = false) email: String?,
        @RequestParam(value = "name", required = false) name: String?,
        @RequestParam(value = "active") active: Boolean?,
        @ParameterObject pageable: Pageable
    ) = userService.findBy(workspaceId, email, name, active, pageable)

    @PostMapping("/workspaces/{workspaceId}/users")
    fun createUser(
        @PathVariable workspaceId: Long,
        @RequestBody userRequest: UserRequest,
    ) = userService.create(workspaceId, userRequest)

    @DeleteMapping("/workspaces/{workspaceId}/users/{userId}")
    fun removeUserFromWorkspace(
        @PathVariable workspaceId: Long,
        @PathVariable userId: Long
    ) = userService.removeUserFromWorkspace(workspaceId, userId)

    @PutMapping("/users/{userId}")
    fun updateUser(
        @PathVariable userId: Long,
        @RequestBody userRequest: UserRequest,
    ) = userService.update(userId, userRequest)

    @DeleteMapping("/users/{userId}")
    fun deleteUser(
        @PathVariable userId: Long,
    ) = userService.deactivate(userId)

    @PostMapping("/users/{userid}/invitations")
    fun sendInvitation(@PathVariable userid: String) = userService.sendInvitation(userid)

    @GetMapping("/users/roles")
    fun getAllRoles() = UserRoleEnum.entries.toTypedArray()
        .filter { it != UserRoleEnum.SUPER_ADMIN }
        .map {
            RoleResponse(it.name, it.roleName, it.description)
        }

    @GetMapping("/users/codes/{code}")
    fun getWorkspaceByCode(@PathVariable code: String) = userService.getWorkspaceByUserCode(code)

    @GetMapping("/users/byEmail")
    fun findUsersByEmail(email: String) = userService.findByEmailResponse(email)

    data class RoleResponse(
        val roleCode: String,
        val roleName: String,
        val description: String,
    )
}