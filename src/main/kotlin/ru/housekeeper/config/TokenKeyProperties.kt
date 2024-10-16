package ru.housekeeper.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("auth.token")
class TokenKeyProperties {
    
    var key = ""
}