package com.xiaohongshu.login.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import com.xiaohongshu.login.model.AuthRequest

/**
 * 应用相关工具类
 * 
 * 提供小红书应用检测、Intent 创建、应用商店跳转等功能。
 * 负责处理与小红书应用的交互和安装检测逻辑。
 */
object AppUtils {

    /** 小红书应用包名 */
    private const val XHS_PACKAGE_NAME = "com.xingin.xhs"
    
    /** 小红书 OAuth 授权 Activity 类名 */
    private const val XHS_LOGIN_ACTIVITY = "com.xingin.xhs.oauth.OAuthActivity"
    
    /** Google Play 商店小红书应用链接 */
    private const val XHS_PLAY_STORE_URL = "market://details?id=$XHS_PACKAGE_NAME"
    
    /** 网页版 Google Play 商店小红书应用链接 */
    private const val XHS_WEB_STORE_URL = "https://play.google.com/store/apps/details?id=$XHS_PACKAGE_NAME"

    /** OAuth 授权请求码 */
    const val REQUEST_CODE_XHS_AUTH = 0x1001

    /** 授权请求 Intent 额外数据键名 */
    const val EXTRA_AUTH_REQUEST = "auth_request"
    const val EXTRA_APP_ID = "app_id"
    const val EXTRA_SCOPE = "scope"
    const val EXTRA_STATE = "state"
    const val EXTRA_CODE_CHALLENGE = "code_challenge"
    const val EXTRA_CODE_CHALLENGE_METHOD = "code_challenge_method"
    
    /** 授权响应 Intent 额外数据键名 */
    const val EXTRA_AUTH_RESPONSE = "auth_response"

    /**
     * 检查小红书应用是否已安装
     * 
     * 通过尝试获取应用包信息来判断小红书应用是否存在。
     * 
     * @param context Android 上下文
     * @return true 如果已安装，false 否则
     */
    @JvmStatic
    fun isXHSAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(XHS_PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 检查小红书应用是否支持 OAuth 授权
     * 
     * 先检查应用是否安装，然后检查是否包含 OAuth 授权 Activity。
     * 
     * @param context Android 上下文
     * @return true 如果支持授权，false 否则
     */
    @JvmStatic
    fun isXHSAppSupportAuth(context: Context): Boolean {
        if (!isXHSAppInstalled(context)) {
            return false
        }

        val intent = Intent().apply {
            component = ComponentName(XHS_PACKAGE_NAME, XHS_LOGIN_ACTIVITY)
        }
        return intent.resolveActivity(context.packageManager) != null
    }

    /**
     * 创建小红书 OAuth 授权 Intent
     * 
     * 构建用于启动小红书授权流程的 Intent，包含授权请求参数。
     * 
     * @param request OAuth 授权请求参数
     * @return 配置好的 Intent 实例
     */
    @JvmStatic
    fun createXHSAuthIntent(request: AuthRequest): Intent {
        return Intent().apply {
            component = ComponentName(XHS_PACKAGE_NAME, XHS_LOGIN_ACTIVITY)
            putExtra(EXTRA_APP_ID, request.appId)
            putExtra(EXTRA_SCOPE, request.scope)
            putExtra(EXTRA_STATE, request.state)
            putExtra(EXTRA_CODE_CHALLENGE, request.codeChallenge)
            putExtra(EXTRA_CODE_CHALLENGE_METHOD, request.codeChallengeMethod)
        }
    }

    /**
     * 创建 Google Play 商店跳转 Intent
     * 
     * 用于引导用户到 Google Play 商店下载小红书应用。
     * 
     * @return 用于跳转到 Play 商店的 Intent
     */
    @JvmStatic
    fun createPlayStoreIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(XHS_PLAY_STORE_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * 创建网页版 Google Play 商店跳转 Intent
     * 
     * 当设备上没有 Play 商店应用时，使用浏览器打开网页版商店。
     * 
     * @return 用于跳转到网页版 Play 商店的 Intent
     */
    @JvmStatic
    fun createWebStoreIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(XHS_WEB_STORE_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * 检查 Intent 是否可以被解析
     * 
     * 判断系统中是否有应用可以处理指定的 Intent。
     * 
     * @param context Android 上下文
     * @param intent 需要检查的 Intent
     * @return true 如果可以被解析，false 否则
     */
    @JvmStatic
    fun canResolveIntent(context: Context, intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }

    /**
     * 构建权限范围字符串
     * 
     * 将权限范围数组转换为逗号分隔的字符串格式。
     * 
     * @param scopes 权限范围数组
     * @return 逗号分隔的权限范围字符串，为空时返回空字符串
     */
    @JvmStatic
    fun buildScopeString(scopes: Array<String>?): String {
        return if (scopes.isNullOrEmpty()) {
            ""
        } else {
            TextUtils.join(",", scopes)
        }
    }
}
