package com.xiaohongshu.login.network

import com.xiaohongshu.login.network.model.TokenRequest
import com.xiaohongshu.login.network.model.TokenResponse
import com.xiaohongshu.login.network.model.UserInfoResponse

interface AuthService {
    fun getAccessToken(request: TokenRequest, callback: ApiCallback<TokenResponse>)
    fun getUserInfo(accessToken: String, callback: ApiCallback<UserInfoResponse>)
    fun refreshToken(
        refreshToken: String,
        clientId: String,
        clientSecret: String,
        callback: ApiCallback<TokenResponse>
    )
}