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

class ApiClient : AuthService {
    
    companion object {
        private const val BASE_URL = "https://open.xiaohongshu.com"
        private const val TOKEN_ENDPOINT = "/oauth/token"
        private const val USER_INFO_ENDPOINT = "/oauth/userinfo"
        private const val REFRESH_TOKEN_ENDPOINT = "/oauth/refresh_token"
        
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private val FORM = "application/x-www-form-urlencoded".toMediaType()
    }
    
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    
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
    
    override fun getUserInfo(accessToken: String, callback: ApiCallback<UserInfoResponse>) {
        val request = Request.Builder()
            .url(BASE_URL + USER_INFO_ENDPOINT)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()
        
        executeRequest(request, UserInfoResponse::class.java, callback)
    }
    
    override fun refreshToken(
        refreshToken: String,
        clientId: String,
        clientSecret: String,
        callback: ApiCallback<TokenResponse>
    ) {
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
    
    private fun <T> executeRequest(
        request: Request,
        responseType: Class<T>,
        callback: ApiCallback<T>
    ) {
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
    
    private fun parseErrorResponse(responseBody: String, httpCode: Int): XHSError {
        return try {
            if (!TextUtils.isEmpty(responseBody)) {
                val errorResponse = gson.fromJson(responseBody, ErrorResponse::class.java)
                XHSError(
                    errorResponse.errorCode,
                    errorResponse.error ?: "Unknown error",
                    errorResponse.errorDescription
                )
            } else {
                XHSError(
                    XHSError.ERROR_NETWORK,
                    "HTTP $httpCode",
                    "Request failed with HTTP code $httpCode"
                )
            }
        } catch (e: Exception) {
            XHSError(
                XHSError.ERROR_NETWORK,
                "HTTP $httpCode",
                "Request failed with HTTP code $httpCode"
            )
        }
    }
    
    private fun postOnMainThread(runnable: () -> Unit) {
        mainHandler.post(runnable)
    }
}