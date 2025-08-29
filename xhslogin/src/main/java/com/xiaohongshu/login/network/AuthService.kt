package com.xiaohongshu.login.network

import com.xiaohongshu.login.network.model.TokenRequest
import com.xiaohongshu.login.network.model.TokenResponse
import com.xiaohongshu.login.network.model.UserInfoResponse

/**
 * OAuth 认证服务接口
 * 
 * 定义了与小红书 OAuth 服务器交互的所有网络接口。
 * 包括令牌获取、用户信息获取和令牌刷新等核心功能。
 * 所有方法都采用异步回调模式，避免阻塞主线程。
 */
interface AuthService {
    
    /**
     * 获取访问令牌
     * 
     * 使用授权码换取访问令牌，完成 OAuth 授权流程的最后一步。
     * 
     * @param request 包含授权码和 PKCE 验证信息的令牌请求
     * @param callback 异步回调，返回令牌响应或错误信息
     */
    fun getAccessToken(request: TokenRequest, callback: ApiCallback<TokenResponse>)
    
    /**
     * 获取用户信息
     * 
     * 使用有效的访问令牌获取用户的基本信息。
     * 
     * @param accessToken 有效的访问令牌
     * @param callback 异步回调，返回用户信息或错误信息
     */
    fun getUserInfo(accessToken: String, callback: ApiCallback<UserInfoResponse>)
    
    /**
     * 刷新访问令牌
     * 
     * 当访问令牌过期时，使用刷新令牌获取新的访问令牌。
     * 
     * @param refreshToken 有效的刷新令牌
     * @param clientId 应用 ID
     * @param clientSecret 应用密钥
     * @param callback 异步回调，返回新的令牌信息或错误信息
     */
    fun refreshToken(refreshToken: String, clientId: String, clientSecret: String, callback: ApiCallback<TokenResponse>)
}
