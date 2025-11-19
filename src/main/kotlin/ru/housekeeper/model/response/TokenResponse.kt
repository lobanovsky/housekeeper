package ru.housekeeper.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenResponse(

    @param:JsonProperty("access_token")
    val accessToken: String,

    @param:JsonProperty("token_type")
    val tokenType: String = "Bearer",

    @param:JsonProperty("expires_in")
    val expiresIn: Int = 2591999,

    val userId: Long?,

    val workspaces: List<Long>,

    )
