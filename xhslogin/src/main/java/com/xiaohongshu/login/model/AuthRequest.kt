package com.xiaohongshu.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * OAuth 授权请求数据模型
 * 
 * 用于封装发送给小红书应用的授权请求参数。
 * 实现了 Parcelable 接口，支持在 Activity 间通过 Intent 传递。
 * 包含了 PKCE (Proof Key for Code Exchange) 安全扩展所需的参数。
 */
@Parcelize
data class AuthRequest(
    /** 第三方应用的唯一标识符，由小红书开放平台分配 */
    var appId: String? = null,
    
    /** 请求的权限范围数组，决定应用可以访问用户的哪些数据 */
    var scope: Array<String>? = null,
    
    /** 随机状态值，用于防止 CSRF 攻击，授权完成后会原样返回 */
    var state: String? = null,
    
    /** PKCE 挑战码，由 code_verifier 通过 SHA256 哈希并 Base64 编码生成 */
    var codeChallenge: String? = null,
    
    /** PKCE 挑战方法，通常为 "S256"，表示使用 SHA256 哈希算法 */
    var codeChallengeMethod: String? = null,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuthRequest

        if (appId != other.appId) return false
        if (scope != null) {
            if (other.scope == null) return false
            if (!scope.contentEquals(other.scope)) return false
        } else if (other.scope != null) return false
        if (state != other.state) return false
        if (codeChallenge != other.codeChallenge) return false
        if (codeChallengeMethod != other.codeChallengeMethod) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appId?.hashCode() ?: 0
        result = 31 * result + (scope?.contentHashCode() ?: 0)
        result = 31 * result + (state?.hashCode() ?: 0)
        result = 31 * result + (codeChallenge?.hashCode() ?: 0)
        result = 31 * result + (codeChallengeMethod?.hashCode() ?: 0)
        return result
    }
}
