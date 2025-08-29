package com.xiaohongshu.login.network.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error")
    val error: String? = null,

    @SerializedName("error_description")
    val errorDescription: String? = null,

    @SerializedName("error_code")
    val errorCode: Int = 0,
) {
    override fun toString(): String {
        return "ErrorResponse(error='$error', errorDescription='$errorDescription', errorCode=$errorCode)"
    }
}
