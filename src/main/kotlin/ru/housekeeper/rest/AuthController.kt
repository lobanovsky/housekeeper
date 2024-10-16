package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.model.response.TokenResponse
import ru.housekeeper.service.AuthService

@Tag(name = "Auth")
@CrossOrigin
@RestController
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun token(
        @RequestBody loginRequest: LoginRequest
    ): TokenResponse {
        val tokenResponse = with(loginRequest) { authService.generateToken(email, password) }
        return TokenResponse(
            accessToken = tokenResponse.token,
            userId = tokenResponse.userId,
            workspaces = tokenResponse.workspaces,
        )
    }

    data class LoginRequest(
        val email: String,
        val password: String
    )

}
