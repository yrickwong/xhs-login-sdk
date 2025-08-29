package com.xiaohongshu.login.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.xiaohongshu.login.callback.XHSLoginCallback
import com.xiaohongshu.login.callback.XHSUserCallback
import com.xiaohongshu.login.model.AuthRequest
import com.xiaohongshu.login.model.AuthResponse
import com.xiaohongshu.login.model.XHSError
import com.xiaohongshu.login.model.XHSScope
import com.xiaohongshu.login.model.XHSUser
import com.xiaohongshu.login.network.ApiCallback
import com.xiaohongshu.login.network.ApiClient
import com.xiaohongshu.login.network.AuthService
import com.xiaohongshu.login.network.model.TokenRequest
import com.xiaohongshu.login.network.model.TokenResponse
import com.xiaohongshu.login.network.model.UserInfoResponse
import com.xiaohongshu.login.security.PKCEHelper
import com.xiaohongshu.login.utils.AppUtils
import com.xiaohongshu.login.utils.StorageUtils

class AuthManager(
    private val context: Context,
    private val appId: String,
    private val appSecret: String,
) {
    companion object {
        private const val TAG = "XHS_AuthManager"
    }
    
    private val authService: AuthService = ApiClient()
    private var pkceHelper: PKCEHelper? = null
    private var loginCallback: XHSLoginCallback? = null

    fun login(activity: Activity, scopes: Array<String>?, callback: XHSLoginCallback) {
        this.loginCallback = callback

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appSecret)) {
            callback.onError(XHSError(XHSError.ERROR_INVALID_PARAMS, "App ID or App Secret is empty"))
            return
        }

        if (!AppUtils.isXHSAppSupportAuth(context)) {
            callback.onError(XHSError(XHSError.ERROR_APP_NOT_INSTALLED, "小红书 App 未安装或不支持授权"))
            return
        }

        val finalScopes = scopes ?: XHSScope.getDefaultScopes()
        startAuthFlow(activity, finalScopes)
    }

    private fun startAuthFlow(activity: Activity, scopes: Array<String>) {
        pkceHelper = PKCEHelper()

        val request = AuthRequest().apply {
            appId = this@AuthManager.appId
            scope = scopes
            state = pkceHelper?.state
            codeChallenge = pkceHelper?.codeChallenge
            codeChallengeMethod = pkceHelper?.codeChallengeMethod
        }

        val intent = AppUtils.createXHSAuthIntent(request)

        try {
            activity.startActivityForResult(intent, AppUtils.REQUEST_CODE_XHS_AUTH)
        } catch (e: Exception) {
            loginCallback?.onError(XHSError(XHSError.ERROR_APP_NOT_INSTALLED, "无法启动小红书授权: ${e.message}"))
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "handleActivityResult: requestCode=$requestCode, resultCode=$resultCode, data=$data")
        
        if (requestCode != AppUtils.REQUEST_CODE_XHS_AUTH) {
            Log.d(TAG, "Not OAuth request code, ignoring")
            return
        }

        val callback = loginCallback ?: run {
            Log.e(TAG, "Login callback is null")
            return
        }

        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "User canceled authorization")
            callback.onCancel()
            return
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            Log.e(TAG, "Authorization failed: resultCode=$resultCode, data=$data")
            callback.onError(XHSError(XHSError.ERROR_AUTH_FAILED, "授权失败"))
            return
        }

        val errCode = data.getIntExtra("err_code", -1)
        val errStr = data.getStringExtra("err_str")
        val code = data.getStringExtra("code")
        val state = data.getStringExtra("state")
        
        Log.d(TAG, "Received auth response: errCode=$errCode, code=$code, state=$state")
        
        val response = AuthResponse(
            errCode = errCode,
            errStr = errStr,
            code = code,
            state = state
        )

        handleAuthResponse(response)
    }

    private fun handleAuthResponse(response: AuthResponse) {
        Log.d(TAG, "handleAuthResponse: errCode=${response.errCode}, code=${response.code}")
        val callback = loginCallback ?: return

        if (!response.isSuccessful) {
            Log.w(TAG, "Auth response not successful: ${response.errCode}")
            when (response.errCode) {
                AuthResponse.ERR_USER_CANCEL -> callback.onCancel()
                AuthResponse.ERR_AUTH_DENIED -> callback.onError(XHSError(XHSError.ERROR_AUTH_FAILED, "用户拒绝授权"))
                AuthResponse.ERR_UNSUPPORTED -> callback.onError(XHSError(XHSError.ERROR_UNSUPPORTED, "不支持的操作"))
                else -> callback.onError(XHSError(XHSError.ERROR_AUTH_FAILED, response.errStr ?: "授权失败"))
            }
            return
        }

        val code = response.code
        val state = response.state

        if (TextUtils.isEmpty(code)) {
            Log.e(TAG, "Authorization code is empty")
            callback.onError(XHSError(XHSError.ERROR_AUTH_FAILED, "未获取到授权码"))
            return
        }

        if (pkceHelper?.verifyState(state) != true) {
            Log.e(TAG, "State verification failed: expected=${pkceHelper?.state}, received=$state")
            callback.onError(XHSError(XHSError.ERROR_AUTH_FAILED, "状态验证失败"))
            return
        }

        Log.d(TAG, "Starting token exchange for code: $code")
        exchangeCodeForToken(code!!)
    }

    private fun exchangeCodeForToken(code: String) {
        val helper = pkceHelper ?: return
        Log.d(TAG, "Exchanging code for token: $code")

        // redirectUri 在新流程中不需要
        val request = TokenRequest(
            code = code,
            clientId = appId,
            clientSecret = appSecret,
            redirectUri = null,
            codeVerifier = helper.codeVerifier,
        )

        authService.getAccessToken(
            request,
            object : ApiCallback<TokenResponse> {
                override fun onSuccess(tokenResponse: TokenResponse) {
                    Log.d(TAG, "Token exchange successful: accessToken=${tokenResponse.accessToken?.take(10)}...")
                    saveTokenInfo(tokenResponse)
                    getUserInfo(tokenResponse.accessToken!!)
                }

                override fun onError(error: XHSError) {
                    Log.e(TAG, "Token exchange failed: ${error.message}")
                    loginCallback?.onError(error)
                }
            },
        )
    }

    private fun saveTokenInfo(tokenResponse: TokenResponse) {
        StorageUtils.saveAccessToken(context, tokenResponse.accessToken)
        StorageUtils.saveRefreshToken(context, tokenResponse.refreshToken)
        StorageUtils.saveTokenExpiresIn(context, tokenResponse.expiresIn)
        StorageUtils.saveOpenId(context, tokenResponse.openId)
    }

    private fun getUserInfo(accessToken: String) {
        Log.d(TAG, "Getting user info with token: ${accessToken.take(10)}...")
        authService.getUserInfo(
            accessToken,
            object : ApiCallback<UserInfoResponse> {
                override fun onSuccess(userInfoResponse: UserInfoResponse) {
                    Log.d(TAG, "User info retrieved: userId=${userInfoResponse.userId}")
                    val user = convertToXHSUser(userInfoResponse, accessToken)
                    StorageUtils.saveUserId(context, user.userId)
                    loginCallback?.onSuccess(user)
                }

                override fun onError(error: XHSError) {
                    Log.e(TAG, "Get user info failed: ${error.message}")
                    loginCallback?.onError(error)
                }
            },
        )
    }

    private fun convertToXHSUser(response: UserInfoResponse, accessToken: String): XHSUser {
        return XHSUser().apply {
            userId = response.userId
            openId = response.openId
            unionId = response.unionId
            nickname = response.nickname
            avatar = response.avatar
            this.accessToken = accessToken
            refreshToken = StorageUtils.getRefreshToken(context)
        }
    }

    fun getUserInfo(accessToken: String, callback: XHSUserCallback) {
        if (TextUtils.isEmpty(accessToken)) {
            callback.onError(XHSError(XHSError.ERROR_INVALID_PARAMS, "Access token is empty"))
            return
        }

        authService.getUserInfo(
            accessToken,
            object : ApiCallback<UserInfoResponse> {
                override fun onSuccess(response: UserInfoResponse) {
                    val user = convertToXHSUser(response, accessToken)
                    callback.onSuccess(user)
                }

                override fun onError(error: XHSError) {
                    callback.onError(error)
                }
            },
        )
    }

    fun refreshToken(callback: XHSUserCallback) {
        val refreshToken = StorageUtils.getRefreshToken(context)
        if (TextUtils.isEmpty(refreshToken)) {
            callback.onError(XHSError(XHSError.ERROR_TOKEN_EXPIRED, "Refresh token is empty"))
            return
        }

        authService.refreshToken(
            refreshToken!!,
            appId,
            appSecret,
            object : ApiCallback<TokenResponse> {
                override fun onSuccess(tokenResponse: TokenResponse) {
                    saveTokenInfo(tokenResponse)
                    getUserInfo(tokenResponse.accessToken!!, callback)
                }

                override fun onError(error: XHSError) {
                    callback.onError(error)
                }
            },
        )
    }

    fun logout() {
        StorageUtils.clearUserData(context)
    }

    fun isLoggedIn(): Boolean {
        return StorageUtils.isLoggedIn(context)
    }
}
