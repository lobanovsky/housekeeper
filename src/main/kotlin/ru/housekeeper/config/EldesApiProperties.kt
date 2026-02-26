package ru.housekeeper.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("eldes-api")
class EldesApiProperties {
    var url = "http://109.173.116.61:8070"
    var email = ""
    var password = ""
}
