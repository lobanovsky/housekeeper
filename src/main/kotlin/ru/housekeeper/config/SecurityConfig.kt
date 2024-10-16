package ru.housekeeper.config

import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfiguration.ALL
import org.springframework.web.cors.CorsConfigurationSource
import ru.housekeeper.security.SecurityFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfig(
    private val securityFilter: SecurityFilter,
    private val userDetailsService: UserDetailsService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    
    internal val allowedPaths = setOf("/v3/**", "/swagger-ui/**", "/login", "/actuator/**")

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            httpBasic { disable() }
            formLogin { disable() }
            logout { disable() }
            cors {
                configurationSource = CorsConfigurationSource {
                    CorsConfiguration().apply {
                        allowedOriginPatterns = listOf(ALL)
                        allowedMethods = listOf(GET, POST, PUT, DELETE, OPTIONS).map { it.name() }
                        allowedHeaders = listOf(ALL)
                        allowCredentials = true
                    }
                }
            }
            authorizeRequests { 
                allowedPaths.forEach {
                    authorize(it, permitAll)
                }
                authorize(anyRequest, authenticated)
            }
            sessionManagement { sessionCreationPolicy = STATELESS }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(securityFilter)
        }
        return http.build()
    }
    
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder(10)
    
    @Bean
    fun authenticationProvider() = DaoAuthenticationProvider(passwordEncoder()).apply { 
        setUserDetailsService(userDetailsService)
        isHideUserNotFoundExceptions = false
    }
    
    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager
    
    @Bean
    fun authenticationEventPublisher() = DefaultAuthenticationEventPublisher(applicationEventPublisher)
}
