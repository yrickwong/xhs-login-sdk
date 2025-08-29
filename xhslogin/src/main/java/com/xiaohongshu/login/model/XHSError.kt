package com.xiaohongshu.login.model

data class XHSError(
    val code: Int,
    val message: String,
    val description: String? = null,
    val cause: Throwable? = null,
) {
    constructor(code: Int, message: String, cause: Throwable?) : this(code, message, null, cause)

    companion object {
        const val ERROR_NETWORK = 1001
        const val ERROR_INVALID_PARAMS = 1002
        const val ERROR_AUTH_FAILED = 1003
        const val ERROR_TOKEN_EXPIRED = 1004
        const val ERROR_APP_NOT_INSTALLED = 1005
        const val ERROR_UNSUPPORTED = 1006
        const val ERROR_UNKNOWN = 1999
    }

    override fun toString(): String {
        return "XHSError(code=$code, message='$message', description='$description')"
    }
}
