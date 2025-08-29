package com.xiaohongshu.login.model

/**
 * 小红书 SDK 错误信息数据模型
 * 
 * 用于封装 SDK 运行过程中可能出现的各种错误情况。
 * 提供统一的错误码和错误描述，便于第三方应用处理异常。
 */
data class XHSError(
    /** 错误码，用于标识具体的错误类型 */
    val code: Int,
    
    /** 错误消息，简短描述错误内容 */
    val message: String,
    
    /** 详细的错误描述信息（可选） */
    val description: String? = null,
    
    /** 引发此错误的原始异常（可选） */
    val cause: Throwable? = null,
) {
    constructor(code: Int, message: String, cause: Throwable?) : this(code, message, null, cause)

    companion object {
        /** 网络错误 - 网络连接失败或超时 */
        const val ERROR_NETWORK = 1001
        
        /** 参数错误 - 传入的参数无效或为空 */
        const val ERROR_INVALID_PARAMS = 1002
        
        /** 授权失败 - OAuth 授权过程失败 */
        const val ERROR_AUTH_FAILED = 1003
        
        /** 令牌过期 - 访问令牌或刷新令牌已过期 */
        const val ERROR_TOKEN_EXPIRED = 1004
        
        /** 应用未安装 - 小红书应用未安装或版本不支持授权 */
        const val ERROR_APP_NOT_INSTALLED = 1005
        
        /** 不支持的操作 - 当前操作不被支持 */
        const val ERROR_UNSUPPORTED = 1006
        
        /** 未知错误 - 其他未预期的错误 */
        const val ERROR_UNKNOWN = 1999
    }

    override fun toString(): String {
        return "XHSError(code=$code, message='$message', description='$description')"
    }
}
