package com.xiaohongshu.login.utils

import android.content.Context
import android.content.SharedPreferences
import com.xiaohongshu.login.security.CryptoUtils

/**
 * 本地存储工具类
 * 
 * 负责管理 SDK 相关数据的本地存储，包括访问令牌、刷新令牌、用户信息等。
 * 使用 SharedPreferences 存储数据，敏感信息（如令牌）会进行加密处理。
 * 提供统一的存储和读取接口，确保数据安全性。
 */
object StorageUtils {

    /** SharedPreferences 文件名 */
    private const val PREF_NAME = "xhs_login_sdk"
    
    /** 访问令牌存储键名 */
    private const val KEY_ACCESS_TOKEN = "access_token"
    
    /** 刷新令牌存储键名 */
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    
    /** 令牌过期时间存储键名 */
    private const val KEY_EXPIRES_IN = "expires_in"
    
    /** 用户 ID 存储键名 */
    private const val KEY_USER_ID = "user_id"
    
    /** OpenID 存储键名 */
    private const val KEY_OPEN_ID = "open_id"
    
    /** 加密密钥 */
    private const val ENCRYPTION_KEY = "xhs_login_key_2023"

    /**
     * 获取 SharedPreferences 实例
     * 
     * @param context Android 上下文
     * @return SharedPreferences 实例
     */
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 保存访问令牌
     * 
     * 将访问令牌加密后存储到本地。
     * 如果加密失败，则保存明文作为后备方案。
     * 
     * @param context Android 上下文
     * @param accessToken 访问令牌
     */
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

    /**
     * 获取访问令牌
     * 
     * 从本地存储中读取并解密访问令牌。
     * 如果解密失败，则尝试直接返回（可能是明文存储）。
     * 
     * @param context Android 上下文
     * @return 访问令牌，如果不存在则返回 null
     */
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

    /**
     * 保存刷新令牌
     * 
     * 将刷新令牌加密后存储到本地。
     * 如果加密失败，则保存明文作为后备方案。
     * 
     * @param context Android 上下文
     * @param refreshToken 刷新令牌
     */
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

    /**
     * 获取刷新令牌
     * 
     * 从本地存储中读取并解密刷新令牌。
     * 如果解密失败，则尝试直接返回（可能是明文存储）。
     * 
     * @param context Android 上下文
     * @return 刷新令牌，如果不存在则返回 null
     */
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

    /**
     * 保存令牌过期时间
     * 
     * 将相对过期时间（秒）转换为绝对时间戳并存储。
     * 
     * @param context Android 上下文
     * @param expiresIn 令牌有效期（秒）
     */
    @JvmStatic
    fun saveTokenExpiresIn(context: Context, expiresIn: Long) {
        val expiresTime = System.currentTimeMillis() + (expiresIn * 1000)
        getSharedPreferences(context).edit()
            .putLong(KEY_EXPIRES_IN, expiresTime)
            .apply()
    }

    /**
     * 检查令牌是否已过期
     * 
     * 比较当前时间与存储的过期时间来判断令牌是否过期。
     * 
     * @param context Android 上下文
     * @return true 如果令牌已过期或不存在，false 否则
     */
    @JvmStatic
    fun isTokenExpired(context: Context): Boolean {
        val expiresTime = getSharedPreferences(context).getLong(KEY_EXPIRES_IN, 0)
        return expiresTime == 0L || System.currentTimeMillis() >= expiresTime
    }

    /**
     * 保存用户 ID
     * 
     * 将用户 ID 以明文形式存储到本地。
     * 
     * @param context Android 上下文
     * @param userId 用户 ID
     */
    @JvmStatic
    fun saveUserId(context: Context, userId: String?) {
        if (userId.isNullOrEmpty()) return

        getSharedPreferences(context).edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    /**
     * 获取用户 ID
     * 
     * 从本地存储中读取用户 ID。
     * 
     * @param context Android 上下文
     * @return 用户 ID，如果不存在则返回 null
     */
    @JvmStatic
    fun getUserId(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_ID, null)
    }

    /**
     * 保存 OpenID
     * 
     * 将 OpenID 以明文形式存储到本地。
     * 
     * @param context Android 上下文
     * @param openId 用户在当前应用中的唯一标识符
     */
    @JvmStatic
    fun saveOpenId(context: Context, openId: String?) {
        if (openId.isNullOrEmpty()) return

        getSharedPreferences(context).edit()
            .putString(KEY_OPEN_ID, openId)
            .apply()
    }

    /**
     * 获取 OpenID
     * 
     * 从本地存储中读取 OpenID。
     * 
     * @param context Android 上下文
     * @return OpenID，如果不存在则返回 null
     */
    @JvmStatic
    fun getOpenId(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_OPEN_ID, null)
    }

    /**
     * 清除所有用户数据
     * 
     * 从本地存储中删除所有相关的用户数据，包括令牌和用户信息。
     * 通常在用户退出登录时调用。
     * 
     * @param context Android 上下文
     */
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

    /**
     * 检查用户是否已登录
     * 
     * 通过检查访问令牌是否存在且未过期来判断用户登录状态。
     * 
     * @param context Android 上下文
     * @return true 如果用户已登录且令牌未过期，false 否则
     */
    @JvmStatic
    fun isLoggedIn(context: Context): Boolean {
        return !getAccessToken(context).isNullOrEmpty() && !isTokenExpired(context)
    }
}
