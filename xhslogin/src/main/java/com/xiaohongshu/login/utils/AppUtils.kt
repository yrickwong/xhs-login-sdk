package com.xiaohongshu.login.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import com.xiaohongshu.login.model.AuthRequest

object AppUtils {

    private const val XHS_PACKAGE_NAME = "com.xingin.xhs"
    private const val XHS_LOGIN_ACTIVITY = "com.xingin.xhs.oauth.OAuthActivity"
    private const val XHS_PLAY_STORE_URL = "market://details?id=$XHS_PACKAGE_NAME"
    private const val XHS_WEB_STORE_URL = "https://play.google.com/store/apps/details?id=$XHS_PACKAGE_NAME"

    // 请求码
    const val REQUEST_CODE_XHS_AUTH = 0x1001

    // Intent extras 常量
    const val EXTRA_AUTH_REQUEST = "auth_request"
    const val EXTRA_AUTH_RESPONSE = "auth_response"

    @JvmStatic
    fun isXHSAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(XHS_PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

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

    @JvmStatic
    fun createXHSAuthIntent(request: AuthRequest): Intent {
        return Intent().apply {
            component = ComponentName(XHS_PACKAGE_NAME, XHS_LOGIN_ACTIVITY)
            putExtra(EXTRA_AUTH_REQUEST, request)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @JvmStatic
    fun createPlayStoreIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(XHS_PLAY_STORE_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @JvmStatic
    fun createWebStoreIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(XHS_WEB_STORE_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @JvmStatic
    fun canResolveIntent(context: Context, intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }

    @JvmStatic
    fun buildScopeString(scopes: Array<String>?): String {
        return if (scopes.isNullOrEmpty()) {
            ""
        } else {
            TextUtils.join(",", scopes)
        }
    }
}
