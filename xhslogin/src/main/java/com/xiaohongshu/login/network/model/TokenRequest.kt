package com.xiaohongshu.login.network.model

data class TokenRequest(
    val code: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String?, // 在新流程中可以为 null
    val codeVerifier: String,
    val grantType: String = "authorization_code",
)
