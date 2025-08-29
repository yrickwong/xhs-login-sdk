package com.xiaohongshu.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.xiaohongshu.login.callback.XHSLoginCallback
import com.xiaohongshu.login.callback.XHSUserCallback
import com.xiaohongshu.login.core.AuthManager
import com.xiaohongshu.login.model.XHSScope
import com.xiaohongshu.login.utils.StorageUtils

class XHSLoginManager private constructor() {

    private var authManager: AuthManager? = null
    private var isConfigured = false

    companion object {
        @Volatile
        private var INSTANCE: XHSLoginManager? = null

        @JvmStatic
        fun getInstance(): XHSLoginManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: XHSLoginManager().also { INSTANCE = it }
            }
        }
    }

    fun configure(context: Context, appId: String, appSecret: String) {
        require(appId.isNotBlank()) { "App ID cannot be null or empty" }
        require(appSecret.isNotBlank()) { "App Secret cannot be null or empty" }

        this.authManager = AuthManager(context, appId, appSecret)
        this.isConfigured = true
    }

    fun login(activity: Activity, callback: XHSLoginCallback) {
        login(activity, null, callback)
    }

    fun login(activity: Activity, scopes: Array<String>?, callback: XHSLoginCallback) {
        checkConfigured()

        val finalScopes = scopes ?: XHSScope.getDefaultScopes()
        authManager?.login(activity, finalScopes, callback)
    }

    fun getUserInfo(accessToken: String, callback: XHSUserCallback) {
        checkConfigured()
        authManager?.getUserInfo(accessToken, callback)
    }

    fun refreshToken(callback: XHSUserCallback) {
        checkConfigured()
        authManager?.refreshToken(callback)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checkConfigured()
        authManager?.handleActivityResult(requestCode, resultCode, data)
    }

    fun logout() {
        checkConfigured()
        authManager?.logout()
    }

    fun isLoggedIn(): Boolean {
        if (!isConfigured) {
            return false
        }
        return authManager?.isLoggedIn() ?: false
    }

    fun getCachedAccessToken(context: Context): String? {
        return StorageUtils.getAccessToken(context)
    }

    fun getCachedUserId(context: Context): String? {
        return StorageUtils.getUserId(context)
    }

    fun getCachedOpenId(context: Context): String? {
        return StorageUtils.getOpenId(context)
    }

    private fun checkConfigured() {
        check(isConfigured && authManager != null) {
            "XHSLoginManager is not configured. Call configure() first."
        }
    }
}
