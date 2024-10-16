package ru.housekeeper.service

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.housekeeper.exception.EntityNotFoundException
import ru.housekeeper.model.entity.User
import ru.housekeeper.model.request.UserRequest
import ru.housekeeper.model.response.UserResponse
import ru.housekeeper.model.response.toResponse
import ru.housekeeper.repository.UserRepository
import ru.housekeeper.security.UserDetailsAdapter
import ru.housekeeper.utils.entityNotfound
import kotlin.let

@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val workspaceService: WorkspaceService,
    @Lazy /* TODO: remove lazy */ private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder
) : org.springframework.security.core.userdetails.UserDetailsService {

    fun findBy(
        workspaceId: Long,
        email: String?,
        name: String?,
        active: Boolean?,
        pageable: Pageable
    ): Page<UserResponse> = userRepository.findAllBy(workspaceId, email, name, active, pageable)
        .map { it.toResponse(workspaceService.findActiveByIds(it.workspaces)) }


    @Transactional
    fun create(
        workspaceId: Long,
        userRequest: UserRequest,
    ): UserResponse {

        if (!EmailValidator.getInstance().isValid(userRequest.email)) {
            throw IllegalArgumentException("Неверный формат email")
        }
        userRepository.findByEmailAndWorkspacesContainsAndActiveIsTrue(
            email = userRequest.email,
            workspaceId = workspaceId
        )?.let {
            throw IllegalArgumentException("Пользователь с таким email в данном окружении уже существует")
        }

        val passwordLength = 10
        val password = generatePassword(passwordLength)

        return userRepository.save(
            User(
                email = userRequest.email,
                name = userRequest.name,
                description = userRequest.description,
                password = password,
                encodedPassword = passwordEncoder.encode(password),
                code = createUniqCode(),
                workspaces = listOf(workspaceId),
                role = userRequest.role,
            )
        ).toResponse(workspaceService.findActiveByIds(setOf(workspaceId)))
    }

    fun generatePassword(count: Int): String =
        RandomStringUtils.random(count, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@!#$%&")

    fun createUniqCode(): String {
        var code = generateCode()
        while (userRepository.findByCode(code) != null) {
            code = generateCode()
        }
        return code
    }

    //generate code for user from 4 digits
    private fun generateCode(): String = RandomStringUtils.random(4, "0123456789")

    fun update(
        userId: Long,
        userRequest: UserRequest
    ): UserResponse {
        val existUser = userRepository.findActiveById(userId)
            ?: throw IllegalArgumentException("Пользователь не найден")

        // Если пытаемся поменять email, то нужно проверить, что такого email нет в данном окружении у другого пользователя
        if (existUser.email != userRequest.email) {
            userRepository.findActiveByEmail(userRequest.email)?.let {
                throw IllegalArgumentException("Пользователь с таким email в данном окружении уже существует")
            }
        }

        existUser.email = userRequest.email
        existUser.name = userRequest.name
        existUser.description = userRequest.description
        existUser.role = userRequest.role

        return userRepository.save(existUser).toResponse(emptySet())
    }

    @Transactional
    fun deactivate(userId: Long) = userRepository.updateActiveById(userId, false)

    fun sendInvitation(userid: String) {
        val existingUser = (userRepository.findActiveById(userid.toLong())
            ?: throw IllegalArgumentException("Пользователь не найден"))
        emailService.sendInvitation(
            existingUser.email,
            existingUser.name,
            existingUser.password,
            existingUser.code
        )
    }

    fun findById(userId: Long): UserResponse = userRepository.findByIdOrNull(userId)?.let {
        it.toResponse(workspaceService.findActiveByIds(it.workspaces))
    } ?: entityNotfound("Пользователь" to userId)

    fun getWorkspaceByUserCode(code: String): UserResponse {
        val user = userRepository.findByCode(code) ?: throw IllegalArgumentException("Пользователь не найден")
        return user.toResponse(workspaceService.findActiveByIds(user.workspaces))
    }

    fun findByEmail(email: String) = userRepository.findActiveByEmail(email)

    fun findByEmailResponse(email: String) = findByEmail(email)?.let {
        it.toResponse(workspaceService.findActiveByIds(it.workspaces))
    }

    override fun loadUserByUsername(username: String) = findByEmail(username)
        ?.let { UserDetailsAdapter(it) }
        ?: throw UsernameNotFoundException("Пользователь c email [$username] не найден")

    @Transactional
    fun removeUserFromWorkspace(workspaceId: Long, userId: Long) {
        val user = userRepository.findByIdOrNull(userId) ?: throw EntityNotFoundException("Пользователь с id $userId не найден")
        user.workspaces -= workspaceId
    }
}