package com.xiaohongshu.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * OAuth 授权响应数据模型
 *
 * 用于封装从小红书应用返回的授权结果。
 * 实现了 Parcelable 接口，支持在 Activity 间通过 Intent 传递。
 * 包含授权码或错误信息，以及状态验证参数。
 */
@Parcelize
data class AuthResponse(
    /** 错误码，0 表示成功，非零值表示不同类型的错误 */
    var errCode: Int = 0,

    /** 错误描述信息，当 errCode 非零时提供具体的错误说明 */
    var errStr: String? = null,

    /** OAuth 授权码，成功授权后由小红书服务器生成的临时凭证 */
    var code: String? = null,

    /** 状态值，用于验证请求的合法性，应与授权请求中的 state 参数一致 */
    var state: String? = null,
) : Parcelable {

    companion object {
        /** 授权成功 */
        const val ERR_OK = 0

        /** 用户取消授权 */
        const val ERR_USER_CANCEL = -2

        /** 用户拒绝授权 */
        const val ERR_AUTH_DENIED = -4

        /** 不支持的操作 */
        const val ERR_UNSUPPORTED = -5
    }

    /**
     * 判断授权是否成功
     *
     * @return true 如果授权成功（errCode == ERR_OK），false 否则
     */
    val isSuccessful: Boolean
        get() = errCode == ERR_OK
}
