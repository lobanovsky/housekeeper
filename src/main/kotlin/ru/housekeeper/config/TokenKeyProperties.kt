package ru.housekeeper.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

//AUTH_TOKEN_KEY env variable as a secret key for JWT
@Component
@ConfigurationProperties("auth.token")
class TokenKeyProperties {
    
    var key = ""
}