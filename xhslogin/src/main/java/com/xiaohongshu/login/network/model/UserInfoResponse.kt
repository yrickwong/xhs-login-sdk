package com.xiaohongshu.login.network.model

import com.google.gson.annotations.SerializedName

/**
 * 用户信息响应数据模型
 * 
 * 封装了从小红书用户信息 API 返回的用户数据。
 * 包含用户的基本信息、身份验证状态等详细信息。
 */
data class UserInfoResponse(
    /** 用户在当前应用中的唯一标识符 */
    @SerializedName("open_id")
    val openId: String? = null,

    /** 用户在开发者账号下的统一标识符 */
    @SerializedName("union_id")
    val unionId: String? = null,

    /** 用户昵称 */
    @SerializedName("nickname")
    val nickname: String? = null,

    /** 用户头像 URL */
    @SerializedName("avatar")
    val avatar: String? = null,

    /** 用户在小红书平台的唯一 ID */
    @SerializedName("user_id")
    val userId: String? = null,

    /** 用户是否已通过身份验证 */
    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    /** 用户性别信息 */
    @SerializedName("gender")
    val gender: String? = null,

    /** 用户所在地理位置 */
    @SerializedName("location")
    val location: String? = null,
)
