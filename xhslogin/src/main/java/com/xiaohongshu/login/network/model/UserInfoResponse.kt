package com.xiaohongshu.login.network.model

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    @SerializedName("open_id")
    val openId: String? = null,

    @SerializedName("union_id")
    val unionId: String? = null,

    @SerializedName("nickname")
    val nickname: String? = null,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("user_id")
    val userId: String? = null,

    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("location")
    val location: String? = null,
)
