package ru.housekeeper.service.gate

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.util.UriComponentsBuilder
import ru.housekeeper.config.EldesApiProperties
import ru.housekeeper.utils.logger
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class EldesApiClient(
    private val props: EldesApiProperties,
    restTemplateBuilder: RestTemplateBuilder,
) {

    private val restTemplate = restTemplateBuilder.build()

    @Volatile
    private var token: String? = null

    // --- DTOs ---

    data class LoginRequest(val email: String, val password: String)

    data class LoginResponse(val accessToken: String)

    data class EventLogEntryResponse(
        val deviceId: String,
        val dateTime: LocalDateTime,
        val status: String,
        val method: String?,
        val userName: String?,
        val phoneNumber: String?,
        val cell: String?,
        val line: String?,
    )

    data class EventLogListResponse(
        val content: List<EventLogEntryResponse>,
        val totalPages: Int,
        val totalElements: Long,
        val number: Int,
        val size: Int,
    )

    // --- Auth ---

    private fun authenticate() {
        logger().info("Authenticating to eldes-api at ${props.url}")
        val response = restTemplate.postForObject(
            "${props.url}/api/auth/login",
            LoginRequest(props.email, props.password),
            LoginResponse::class.java,
        ) ?: throw IllegalStateException("eldes-api login returned null")
        token = response.accessToken
        logger().info("eldes-api authentication successful")
    }

    private fun authHeaders(): HttpHeaders {
        if (token == null) authenticate()
        return HttpHeaders().apply {
            setBearerAuth(token!!)
        }
    }

    // --- Event log ---

    fun fetchEventLog(
        deviceId: String,
        from: LocalDate?,
        page: Int,
        pageSize: Int,
    ): EventLogListResponse {
        return try {
            doFetchEventLog(deviceId, from, page, pageSize)
        } catch (e: HttpClientErrorException.Unauthorized) {
            logger().warn("eldes-api token expired, re-authenticating")
            token = null
            authenticate()
            doFetchEventLog(deviceId, from, page, pageSize)
        }
    }

    private fun doFetchEventLog(
        deviceId: String,
        from: LocalDate?,
        page: Int,
        pageSize: Int,
    ): EventLogListResponse {
        val uriBuilder = UriComponentsBuilder
            .fromHttpUrl("${props.url}/api/event-logs")
            .queryParam("deviceId", deviceId)
            .queryParam("page", page)
            .queryParam("size", pageSize)

        if (from != null) uriBuilder.queryParam("from", from.toString())

        val uri = uriBuilder.toUriString()
        val response = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            HttpEntity<Unit>(authHeaders()),
            EventLogListResponse::class.java,
        )
        return response.body ?: throw IllegalStateException("eldes-api returned null body for $uri")
    }
}
