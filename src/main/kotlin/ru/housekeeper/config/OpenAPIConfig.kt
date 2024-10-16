package ru.housekeeper.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.PathMatcher

@Configuration
class OpenAPIConfig(
    private val securityConfig: SecurityConfig,
    private val pathMatcher: PathMatcher
) {

    private val schemeName = "jwt"
    private val requirement = SecurityRequirement().addList(schemeName)
    private val scheme = SecurityScheme()
        .name(schemeName)
        .type(HTTP)
        .scheme("bearer")

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .components(Components())
        .info(
            Info().title("Housekeeper Application API").version("v0")
        )


    @Bean
    fun securityOpenApiCustomizer() = OpenApiCustomizer { api ->
        api.components.addSecuritySchemes(schemeName, scheme)
        api.paths
            .filterSecured()
            .values
            .flatMap { it.readOperations() }
            .forEach { it.addSecurityItem(requirement) }
    }

    private fun Paths.filterSecured() = filterKeys { path -> path.noneMatches(securityConfig.allowedPaths) }

    private fun String.noneMatches(paths: Collection<String>) = paths.none { pathMatcher.match(it, this) }
}