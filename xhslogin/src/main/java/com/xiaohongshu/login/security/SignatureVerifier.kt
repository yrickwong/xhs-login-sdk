package com.xiaohongshu.login.security

import android.text.TextUtils
import java.util.*

/**
 * API 签名验证工具类
 * 用于对 API 请求参数进行签名验证，提供额外的安全保护
 * 注意：当前 OAuth 流程使用 HTTPS + PKCE，不需要此签名验证
 * 此类为未来可能的 API 签名需求预留
 */
object SignatureVerifier {
    
    @JvmStatic
    fun verifySignature(params: Map<String, String>, signature: String, appSecret: String): Boolean {
        if (TextUtils.isEmpty(signature) || TextUtils.isEmpty(appSecret)) {
            return false
        }
        
        val expectedSignature = generateSignature(params, appSecret)
        return signature == expectedSignature
    }
    
    @JvmStatic
    fun generateSignature(params: Map<String, String>, appSecret: String): String {
        val sortedParams = TreeMap(params)
        
        val signBuilder = StringBuilder()
        for ((key, value) in sortedParams) {
            if (!TextUtils.isEmpty(value)) {
                signBuilder.append(key)
                    .append("=")
                    .append(value)
                    .append("&")
            }
        }
        
        if (signBuilder.isNotEmpty()) {
            signBuilder.deleteCharAt(signBuilder.length - 1)
        }
        
        signBuilder.append(appSecret)
        
        return CryptoUtils.sha256(signBuilder.toString())
    }
}