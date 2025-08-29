package com.xiaohongshu.login.network.model

/**
 * OAuth 令牌请求数据模型
 * 
 * 用于向小红书 OAuth 服务器请求访问令牌的参数封装。
 * 支持 PKCE (Proof Key for Code Exchange) 安全扩展。
 */
data class TokenRequest(
    /** OAuth 授权码，从授权服务器获得的临时凭证 */
    val code: String,
    
    /** 客户端 ID，即应用在小红书开放平台的标识符 */
    val clientId: String,
    
    /** 客户端密钥，用于验证应用身份的密钥 */
    val clientSecret: String,
    
    /** 重定向 URI，在 App-to-App 流程中可以为 null */
    val redirectUri: String?, // 在新流程中可以为 null
    
    /** PKCE 代码验证器，用于验证授权请求的合法性 */
    val codeVerifier: String,
    
    /** 授权类型，固定为 "authorization_code" */
    val grantType: String = "authorization_code",
)
