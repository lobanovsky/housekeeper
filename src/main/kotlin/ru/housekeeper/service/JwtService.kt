package ru.housekeeper.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.SIG.HS256
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import ru.housekeeper.config.TokenKeyProperties
import ru.housekeeper.security.UserDetailsAdapter
import ru.housekeeper.service.JwtService.ClaimNames.EMAIL
import ru.housekeeper.service.JwtService.ClaimNames.ID
import ru.housekeeper.service.JwtService.ClaimNames.ROLE
import ru.housekeeper.utils.asDate
import java.time.LocalDateTime
import java.util.*

@Service
class JwtService(keyProperties: TokenKeyProperties) {

    object ClaimNames {
        const val ID = "id"
        const val EMAIL = "email"
        const val ROLE = "role"
    }

    private val secretKey = Decoders.BASE64.decode(keyProperties.key).let {
        Keys.hmacShaKeyFor(it)
    }

    fun generateToken(user: UserDetailsAdapter): String = generateToken(user) {
        put(ID, user.id)
        put(EMAIL, user.email)
        put(ROLE, user.role)
    }

    private fun generateToken(user: UserDetailsAdapter, claimsBuilder: MutableMap<String, Any?>.() -> Unit) = Jwts.builder()
        .claims(buildMap(claimsBuilder))
        .subject(user.email)
        .issuedAt(Date())
        .expiration(LocalDateTime.now().plusMinutes(10).asDate())
        .signWith(secretKey, HS256)
        .compact()

    fun extractLogin(token: String): String = extractClaim(token) { it[EMAIL].toString() }

    private fun extractAllClaims(token: String): Claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .payload

    private fun <T> extractClaim(token: String, extractor: (Claims) -> T) = extractor(extractAllClaims(token))

    fun isExpired(token: String) = extractClaim(token) { it.expiration }.before(Date())

    fun isValid(token: String, userDetails: UserDetails) = extractClaim(token) { it.subject }
        .let { it == userDetails.username && !isExpired(token) }
}