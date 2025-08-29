package com.xiaohongshu.login.network

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.google.gson.Gson
import com.xiaohongshu.login.model.XHSError
import com.xiaohongshu.login.network.model.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API 客户端实现类
 * 
 * 负责与小红书 OAuth 服务器进行 HTTP 通信的具体实现。
 * 使用 OkHttp 作为底层 HTTP 客户端，支持异步请求和自动重试。
 * 所有网络回调都会切换到主线程执行，便于 UI 更新。
 */
class ApiClient : AuthService {

    companion object {
        /** 小红书开放平台 API 基础 URL */
        private const val BASE_URL = "http://10.0.2.2:8080"
        
        /** 获取访问令牌的端点路径 */
        private const val TOKEN_ENDPOINT = "/oauth/token"
        
        /** 获取用户信息的端点路径 */
        private const val USER_INFO_ENDPOINT = "/oauth/userinfo"
        
        /** 刷新访问令牌的端点路径 */
        private const val REFRESH_TOKEN_ENDPOINT = "/oauth/refresh_token"

        /** JSON 请求内容类型 */
        private val JSON = "application/json; charset=utf-8".toMediaType()
        
        /** 表单请求内容类型 */
        private val FORM = "application/x-www-form-urlencoded".toMediaType()
    }

    /** HTTP 客户端实例，配置了连接和读取超时 */
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** JSON 序列化/反序列化工具 */
    private val gson = Gson()
    
    /** 主线程处理器，用于将网络回调切换到主线程 */
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 获取访问令牌
     * 
     * 使用授权码换取访问令牌，支持 PKCE 安全扩展。
     * 请求使用 application/x-www-form-urlencoded 格式。
     * 
     * @param request 令牌请求参数
     * @param callback 网络请求回调
     */
    override fun getAccessToken(request: TokenRequest, callback: ApiCallback<TokenResponse>) {
        val formBuilder = FormBody.Builder()
            .add("grant_type", request.grantType)
            .add("code", request.code)
            .add("client_id", request.clientId)
            .add("client_secret", request.clientSecret)
            .add("code_verifier", request.codeVerifier)

        // 只有在 redirect_uri 不为空时才添加
        if (!TextUtils.isEmpty(request.redirectUri)) {
            formBuilder.add("redirect_uri", request.redirectUri!!)
        }

        val httpRequest = Request.Builder()
            .url(BASE_URL + TOKEN_ENDPOINT)
            .post(formBuilder.build())
            .build()

        executeRequest(httpRequest, TokenResponse::class.java, callback)
    }

    /**
     * 获取用户信息
     * 
     * 使用访问令牌从小红书服务器获取用户的基本信息。
     * 使用 Bearer 认证方式传递访问令牌。
     * 
     * @param accessToken 有效的访问令牌
     * @param callback 网络请求回调
     */
    override fun getUserInfo(accessToken: String, callback: ApiCallback<UserInfoResponse>) {
        val request = Request.Builder()
            .url(BASE_URL + USER_INFO_ENDPOINT)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        executeRequest(request, UserInfoResponse::class.java, callback)
    }

    /**
     * 刷新访问令牌
     * 
     * 使用刷新令牌获取新的访问令牌。
     * 当访问令牌过期时可以使用此方法续期。
     * 
     * @param refreshToken 有效的刷新令牌
     * @param clientId 应用 ID
     * @param clientSecret 应用密钥
     * @param callback 网络请求回调
     */
    override fun refreshToken(refreshToken: String, clientId: String, clientSecret: String, callback: ApiCallback<TokenResponse>) {
        val formBuilder = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", clientId)
            .add("client_secret", clientSecret)

        val request = Request.Builder()
            .url(BASE_URL + REFRESH_TOKEN_ENDPOINT)
            .post(formBuilder.build())
            .build()

        executeRequest(request, TokenResponse::class.java, callback)
    }

    /**
     * 执行 HTTP 请求
     * 
     * 使用 OkHttp 异步执行 HTTP 请求，并将响应结果切换到主线程回调。
     * 自动处理网络错误、HTTP 错误和 JSON 解析错误。
     * 
     * @param T 响应数据类型
     * @param request HTTP 请求对象
     * @param responseType 响应数据类型 Class
     * @param callback 网络请求回调
     */
    private fun <T> executeRequest(request: Request, responseType: Class<T>, callback: ApiCallback<T>) {
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val error = XHSError(XHSError.ERROR_NETWORK, "Network request failed", e)
                postOnMainThread { callback.onError(error) }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        if (TextUtils.isEmpty(responseBody)) {
                            val error = XHSError(XHSError.ERROR_UNKNOWN, "Empty response body")
                            postOnMainThread { callback.onError(error) }
                            return
                        }

                        val data = gson.fromJson(responseBody, responseType)
                        postOnMainThread { callback.onSuccess(data) }
                    } else {
                        val error = parseErrorResponse(responseBody, response.code)
                        postOnMainThread { callback.onError(error) }
                    }
                } catch (e: Exception) {
                    val error = XHSError(XHSError.ERROR_UNKNOWN, "Failed to parse response", e)
                    postOnMainThread { callback.onError(error) }
                } finally {
                    response.body?.close()
                }
            }
        })
    }

    /**
     * 解析错误响应
     * 
     * 将服务器返回的错误响应解析为 XHSError 对象。
     * 如果响应体为空或解析失败，则返回通用的 HTTP 错误。
     * 
     * @param responseBody 响应体字符串
     * @param httpCode HTTP 状态码
     * @return 解析后的错误对象
     */
    private fun parseErrorResponse(responseBody: String, httpCode: Int): XHSError {
        return try {
            if (!TextUtils.isEmpty(responseBody)) {
                val errorResponse = gson.fromJson(responseBody, ErrorResponse::class.java)
                XHSError(
                    errorResponse.errorCode,
                    errorResponse.error ?: "Unknown error",
                    errorResponse.errorDescription,
                )
            } else {
                XHSError(
                    XHSError.ERROR_NETWORK,
                    "HTTP $httpCode",
                    "Request failed with HTTP code $httpCode",
                )
            }
        } catch (e: Exception) {
            XHSError(
                XHSError.ERROR_NETWORK,
                "HTTP $httpCode",
                "Request failed with HTTP code $httpCode",
            )
        }
    }

    /**
     * 在主线程执行任务
     * 
     * 将网络回调从子线程切换到主线程执行，便于 UI 更新。
     * 
     * @param runnable 需要在主线程执行的任务
     */
    private fun postOnMainThread(runnable: () -> Unit) {
        mainHandler.post(runnable)
    }
}
