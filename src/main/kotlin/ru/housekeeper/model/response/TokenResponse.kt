package ru.housekeeper.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenResponse(

    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("token_type")
    val tokenType: String = "Bearer",

    @JsonProperty("expires_in")
    val expiresIn: Int = 2591999,

    val userId: Long?,

    val workspaces: List<Long>,

    )
