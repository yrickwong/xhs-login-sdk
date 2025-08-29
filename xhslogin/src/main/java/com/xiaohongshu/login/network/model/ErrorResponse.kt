package com.xiaohongshu.login.network.model

import com.google.gson.annotations.SerializedName

/**
 * 网络错误响应数据模型
 * 
 * 封装了从小红书 OAuth 服务器返回的错误信息。
 * 用于解析和处理 HTTP 错误响应，提供结构化的错误信息。
 */
data class ErrorResponse(
    /** 错误类型标识符，通常为标准的 OAuth 错误代码 */
    @SerializedName("error")
    val error: String? = null,

    /** 详细的错误描述信息，帮助理解错误原因 */
    @SerializedName("error_description")
    val errorDescription: String? = null,

    /** 数字错误码，用于程序化处理不同类型的错误 */
    @SerializedName("error_code")
    val errorCode: Int = 0,
) {
    override fun toString(): String {
        return "ErrorResponse(error='$error', errorDescription='$errorDescription', errorCode=$errorCode)"
    }
}
