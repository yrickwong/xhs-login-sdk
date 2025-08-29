package com.xiaohongshu.login.network.model

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String? = null,
    
    @SerializedName("token_type")
    val tokenType: String? = null,
    
    @SerializedName("expires_in")
    val expiresIn: Long = 0,
    
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    
    @SerializedName("scope")
    val scope: String? = null,
    
    @SerializedName("open_id")
    val openId: String? = null,
    
    @SerializedName("union_id")
    val unionId: String? = null
)