package com.xiaohongshu.login.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

/**
 * PKCE (Proof Key for Code Exchange) 助手类
 * 
 * 实现 OAuth 2.0 PKCE 安全扩展，防止授权码拦截攻击。
 * 自动生成代码验证器、挑战码和状态值，提供授权流程的安全保护。
 * 
 * PKCE 工作流程：
 * 1. 生成随机的 code_verifier
 * 2. 使用 SHA256 对 code_verifier 进行哈希得到 code_challenge
 * 3. 在授权请求中发送 code_challenge
 * 4. 在令牌请求中发送 code_verifier 进行验证
 */
class PKCEHelper {

    companion object {
        /** 挑战方法，使用 SHA256 哈希算法 */
        private const val CODE_CHALLENGE_METHOD = "S256"
        
        /** 代码验证器长度，符合 RFC 7636 建议的最大长度 */
        private const val CODE_VERIFIER_LENGTH = 128
        
        /** 代码验证器字符集，包含 URL 安全的字符 */
        private const val CODE_VERIFIER_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
    }

    /** PKCE 代码验证器，用于令牌交换时的验证 */
    val codeVerifier: String
    
    /** PKCE 代码挑战，code_verifier 的 SHA256 哈希值 */
    val codeChallenge: String
    
    /** 挑战方法，固定为 "S256" */
    val codeChallengeMethod: String = CODE_CHALLENGE_METHOD
    
    /** 状态值，用于防止 CSRF 攻击 */
    val state: String

    init {
        codeVerifier = generateCodeVerifier()
        codeChallenge = generateCodeChallenge(codeVerifier)
        state = generateState()
    }

    /**
     * 生成 PKCE 代码验证器
     * 
     * 使用安全随机数生成器生成 128 位随机字符串。
     * 字符集符合 RFC 7636 规范，使用 URL 安全字符。
     * 
     * @return 128 位随机字符串
     */
    private fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        return (1..CODE_VERIFIER_LENGTH)
            .map { CODE_VERIFIER_CHARSET[secureRandom.nextInt(CODE_VERIFIER_CHARSET.length)] }
            .joinToString("")
    }

    /**
     * 生成 PKCE 代码挑战
     * 
     * 对代码验证器进行 SHA-256 哈希，然后使用 Base64 URL 安全编码。
     * 编码时不包含填充和换行符，符合 PKCE 规范要求。
     * 
     * @param verifier 代码验证器字符串
     * @return Base64 URL 安全编码的挑战码
     */
    private fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val challengeBytes = digest.digest(verifier.toByteArray())
        return Base64.encodeToString(challengeBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * 生成随机状态值
     * 
     * 使用 UUID 生成随机状态值，用于防止 CSRF 攻击。
     * 移除连字符以简化状态值格式。
     * 
     * @return 32 位随机字符串
     */
    private fun generateState(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 验证状态值
     * 
     * 比较授权响应中的状态值与本地生成的状态值是否一致。
     * 用于防止 CSRF 攻击和确保授权请求的合法性。
     * 
     * @param receivedState 从授权响应中接收到的状态值
     * @return true 如果状态值匹配，false 否则
     */
    fun verifyState(receivedState: String?): Boolean {
        return state == receivedState
    }
}
