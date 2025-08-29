package com.xiaohongshu.login.network.model

import com.google.gson.annotations.SerializedName

/**
 * OAuth 令牌响应数据模型
 * 
 * 封装了从小红书 OAuth 服务器返回的令牌信息。
 * 包含访问令牌、刷新令牌、用户标识符等关键信息。
 */
data class TokenResponse(
    /** 访问令牌，用于调用小红书 API 的凭证 */
    @SerializedName("access_token")
    val accessToken: String? = null,

    /** 令牌类型，通常为 "Bearer" */
    @SerializedName("token_type")
    val tokenType: String? = null,

    /** 访问令牌的有效期（秒），从获取时刻开始计算 */
    @SerializedName("expires_in")
    val expiresIn: Long = 0,

    /** 刷新令牌，用于获取新的访问令牌 */
    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    /** 授权范围，以空格分隔的权限列表 */
    @SerializedName("scope")
    val scope: String? = null,

    /** 用户在当前应用中的唯一标识符 */
    @SerializedName("open_id")
    val openId: String? = null,

    /** 用户在开发者账号下的统一标识符 */
    @SerializedName("union_id")
    val unionId: String? = null,
)
