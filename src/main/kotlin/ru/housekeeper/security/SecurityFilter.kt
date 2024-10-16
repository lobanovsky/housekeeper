package ru.housekeeper.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.housekeeper.service.JwtService


@Component
class SecurityFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val requestedPath = request.servletPath

        val authHeader = request.getHeader(AUTHORIZATION)
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response)
            return
        }
        val token = authHeader.substringAfter("Bearer").trim()

        val email = runCatching { jwtService.extractLogin(token) }.getOrElse { throw AccessDeniedException("Token is not valid", it) }

        if (email.isNotBlank() && SecurityContextHolder.getContext().authentication == null) {
            val userDetails = userDetailsService.loadUserByUsername(email) ?: throw AccessDeniedException("User not found")

            if (jwtService.isValid(token, userDetails)) {
                val context = SecurityContextHolder.createEmptyContext()
                val authToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                context.authentication = authToken
                SecurityContextHolder.setContext(context)
            }
        }

        filterChain.doFilter(request, response)
    }
}
