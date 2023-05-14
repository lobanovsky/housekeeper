package ru.tsn.housekeeper.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .components(Components())
        .info(
            Info().title("Housekeeper Application API").version("v0")
        )

}