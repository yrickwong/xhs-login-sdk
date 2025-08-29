package com.xiaohongshu.login.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.xiaohongshu.login.security.CryptoUtils

object StorageUtils {
    
    private const val PREF_NAME = "xhs_login_sdk"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_EXPIRES_IN = "expires_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_OPEN_ID = "open_id"
    private const val ENCRYPTION_KEY = "xhs_login_key_2023"
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    @JvmStatic
    fun saveAccessToken(context: Context, accessToken: String?) {
        if (accessToken.isNullOrEmpty()) return
        
        try {
            val encryptedToken = CryptoUtils.encrypt(accessToken, ENCRYPTION_KEY)
            getSharedPreferences(context).edit()
                .putString(KEY_ACCESS_TOKEN, encryptedToken)
                .apply()
        } catch (e: Exception) {
            // 如果加密失败，直接存储明文（在生产环境中应该处理此错误）
            getSharedPreferences(context).edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .apply()
        }
    }
    
    @JvmStatic
    fun getAccessToken(context: Context): String? {
        val encryptedToken = getSharedPreferences(context).getString(KEY_ACCESS_TOKEN, null)
        if (encryptedToken.isNullOrEmpty()) return null
        
        return try {
            CryptoUtils.decrypt(encryptedToken, ENCRYPTION_KEY)
        } catch (e: Exception) {
            // 如果解密失败，可能是明文存储的，直接返回
            encryptedToken
        }
    }
    
    @JvmStatic
    fun saveRefreshToken(context: Context, refreshToken: String?) {
        if (refreshToken.isNullOrEmpty()) return
        
        try {
            val encryptedToken = CryptoUtils.encrypt(refreshToken, ENCRYPTION_KEY)
            getSharedPreferences(context).edit()
                .putString(KEY_REFRESH_TOKEN, encryptedToken)
                .apply()
        } catch (e: Exception) {
            getSharedPreferences(context).edit()
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply()
        }
    }
    
    @JvmStatic
    fun getRefreshToken(context: Context): String? {
        val encryptedToken = getSharedPreferences(context).getString(KEY_REFRESH_TOKEN, null)
        if (encryptedToken.isNullOrEmpty()) return null
        
        return try {
            CryptoUtils.decrypt(encryptedToken, ENCRYPTION_KEY)
        } catch (e: Exception) {
            encryptedToken
        }
    }
    
    @JvmStatic
    fun saveTokenExpiresIn(context: Context, expiresIn: Long) {
        val expiresTime = System.currentTimeMillis() + (expiresIn * 1000)
        getSharedPreferences(context).edit()
            .putLong(KEY_EXPIRES_IN, expiresTime)
            .apply()
    }
    
    @JvmStatic
    fun isTokenExpired(context: Context): Boolean {
        val expiresTime = getSharedPreferences(context).getLong(KEY_EXPIRES_IN, 0)
        return expiresTime == 0L || System.currentTimeMillis() >= expiresTime
    }
    
    @JvmStatic
    fun saveUserId(context: Context, userId: String?) {
        if (userId.isNullOrEmpty()) return
        
        getSharedPreferences(context).edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    @JvmStatic
    fun getUserId(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_ID, null)
    }
    
    @JvmStatic
    fun saveOpenId(context: Context, openId: String?) {
        if (openId.isNullOrEmpty()) return
        
        getSharedPreferences(context).edit()
            .putString(KEY_OPEN_ID, openId)
            .apply()
    }
    
    @JvmStatic
    fun getOpenId(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_OPEN_ID, null)
    }
    
    @JvmStatic
    fun clearUserData(context: Context) {
        getSharedPreferences(context).edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_IN)
            .remove(KEY_USER_ID)
            .remove(KEY_OPEN_ID)
            .apply()
    }
    
    @JvmStatic
    fun isLoggedIn(context: Context): Boolean {
        return !getAccessToken(context).isNullOrEmpty() && !isTokenExpired(context)
    }
}