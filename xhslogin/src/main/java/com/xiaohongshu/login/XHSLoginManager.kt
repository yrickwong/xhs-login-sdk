package com.xiaohongshu.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.xiaohongshu.login.callback.XHSLoginCallback
import com.xiaohongshu.login.callback.XHSUserCallback
import com.xiaohongshu.login.core.AuthManager
import com.xiaohongshu.login.model.XHSScope
import com.xiaohongshu.login.utils.StorageUtils

/**
 * 小红书 OAuth 登录 SDK 主入口管理类
 * 
 * 提供小红书第三方应用登录授权功能，支持完整的 OAuth 2.0 授权码流程。
 * 使用单例模式，确保全局只有一个实例。
 * 
 * 主要功能：
 * - 配置应用凭证(App ID 和 App Secret)
 * - 启动登录授权流程
 * - 获取用户信息
 * - 刷新访问令牌
 * - 处理授权回调
 * - 退出登录
 * - 管理本地缓存的令牌和用户数据
 */
class XHSLoginManager private constructor() {

    /** 核心授权管理器，处理具体的授权逻辑 */
    private var authManager: AuthManager? = null
    
    /** 标识 SDK 是否已完成配置 */
    private var isConfigured = false

    companion object {
        @Volatile
        private var INSTANCE: XHSLoginManager? = null

        /**
         * 获取 XHSLoginManager 单例实例
         * 
         * 使用双重检查锁定模式确保线程安全的单例创建
         * 
         * @return XHSLoginManager 实例
         */
        @JvmStatic
        fun getInstance(): XHSLoginManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: XHSLoginManager().also { INSTANCE = it }
            }
        }
    }

    /**
     * 配置 SDK
     * 
     * 使用小红书开放平台提供的应用凭证初始化 SDK。
     * 必须在使用其他功能前调用此方法。
     * 
     * @param context Android 上下文，用于初始化内部组件
     * @param appId 小红书开放平台分配的应用 ID
     * @param appSecret 小红书开放平台分配的应用密钥
     * @throws IllegalArgumentException 当 appId 或 appSecret 为空时抛出
     */
    fun configure(context: Context, appId: String, appSecret: String) {
        require(appId.isNotBlank()) { "App ID cannot be null or empty" }
        require(appSecret.isNotBlank()) { "App Secret cannot be null or empty" }

        this.authManager = AuthManager(context, appId, appSecret)
        this.isConfigured = true
    }

    /**
     * 启动登录授权流程（使用默认权限范围）
     * 
     * 使用默认的权限范围启动 OAuth 授权流程。
     * 将跳转到小红书 App 进行用户授权。
     * 
     * @param activity 发起登录的 Activity，用于接收授权回调
     * @param callback 登录结果回调
     */
    fun login(activity: Activity, callback: XHSLoginCallback) {
        login(activity, null, callback)
    }

    /**
     * 启动登录授权流程（指定权限范围）
     * 
     * 使用指定的权限范围启动 OAuth 授权流程。
     * 将跳转到小红书 App 进行用户授权。
     * 
     * @param activity 发起登录的 Activity，用于接收授权回调
     * @param scopes 请求的权限范围数组，为 null 时使用默认权限
     * @param callback 登录结果回调
     * @throws IllegalStateException 当 SDK 未配置时抛出
     */
    fun login(activity: Activity, scopes: Array<String>?, callback: XHSLoginCallback) {
        checkConfigured()

        val finalScopes = scopes ?: XHSScope.getDefaultScopes()
        authManager?.login(activity, finalScopes, callback)
    }

    /**
     * 获取用户信息
     * 
     * 使用访问令牌从小红书服务器获取用户的基本信息。
     * 
     * @param accessToken 有效的访问令牌
     * @param callback 获取用户信息的回调
     * @throws IllegalStateException 当 SDK 未配置时抛出
     */
    fun getUserInfo(accessToken: String, callback: XHSUserCallback) {
        checkConfigured()
        authManager?.getUserInfo(accessToken, callback)
    }

    /**
     * 刷新访问令牌
     * 
     * 使用本地保存的刷新令牌获取新的访问令牌。
     * 当访问令牌过期时可以调用此方法续期。
     * 
     * @param callback 刷新令牌的回调
     * @throws IllegalStateException 当 SDK 未配置时抛出
     */
    fun refreshToken(callback: XHSUserCallback) {
        checkConfigured()
        authManager?.refreshToken(callback)
    }

    /**
     * 处理授权回调结果
     * 
     * 在 Activity 的 onActivityResult 方法中调用，
     * 用于处理从小红书 App 返回的授权结果。
     * 
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的 Intent 数据
     * @throws IllegalStateException 当 SDK 未配置时抛出
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checkConfigured()
        authManager?.handleActivityResult(requestCode, resultCode, data)
    }

    /**
     * 退出登录
     * 
     * 清除本地保存的所有用户数据和令牌信息。
     * 调用后用户需要重新登录才能使用相关功能。
     * 
     * @throws IllegalStateException 当 SDK 未配置时抛出
     */
    fun logout() {
        checkConfigured()
        authManager?.logout()
    }

    /**
     * 检查用户是否已登录
     * 
     * 根据本地保存的令牌信息判断用户是否已经登录。
     * 注意：此方法只检查本地状态，不验证令牌是否仍然有效。
     * 
     * @return true 如果本地存在有效的令牌信息，false 否则
     */
    fun isLoggedIn(): Boolean {
        if (!isConfigured) {
            return false
        }
        return authManager?.isLoggedIn() ?: false
    }

    /**
     * 获取缓存的访问令牌
     * 
     * 从本地存储中读取之前保存的访问令牌。
     * 返回的令牌可能已过期，使用前建议先检查有效性。
     * 
     * @param context Android 上下文
     * @return 缓存的访问令牌，如果不存在则返回 null
     */
    fun getCachedAccessToken(context: Context): String? {
        return StorageUtils.getAccessToken(context)
    }

    /**
     * 获取缓存的用户 ID
     * 
     * 从本地存储中读取之前保存的用户 ID。
     * 
     * @param context Android 上下文
     * @return 缓存的用户 ID，如果不存在则返回 null
     */
    fun getCachedUserId(context: Context): String? {
        return StorageUtils.getUserId(context)
    }

    /**
     * 获取缓存的 OpenID
     * 
     * 从本地存储中读取之前保存的 OpenID。
     * OpenID 是用户在当前应用中的唯一标识符。
     * 
     * @param context Android 上下文
     * @return 缓存的 OpenID，如果不存在则返回 null
     */
    fun getCachedOpenId(context: Context): String? {
        return StorageUtils.getOpenId(context)
    }

    /**
     * 检查 SDK 是否已经正确配置
     * 
     * @throws IllegalStateException 当 SDK 未配置时抛出
     */
    private fun checkConfigured() {
        check(isConfigured && authManager != null) {
            "XHSLoginManager is not configured. Call configure() first."
        }
    }
}
