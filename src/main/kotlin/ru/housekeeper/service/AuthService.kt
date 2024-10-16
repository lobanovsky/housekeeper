package ru.housekeeper.service

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import ru.housekeeper.security.UserDetailsAdapter

@Service
class AuthService(
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    data class TokenResponse(
        val token: String,
        val userId: Long?,
        val workspaces: List<Long>,
    )

    fun generateToken(email: String, password: String): TokenResponse {
        val authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
        val userDetails = authentication.principal as UserDetailsAdapter
        val token = jwtService.generateToken(userDetails)
        return TokenResponse(token, userDetails.id, userDetails.workspaces)
    }
}
