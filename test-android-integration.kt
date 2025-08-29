#!/usr/bin/env kotlin

/**
 * Android SDK 与 OAuth 服务器集成测试脚本
 * 
 * 这个脚本模拟Android SDK的网络请求，测试与本地OAuth服务器的连接
 */

@file:DependsOn("com.squareup.okhttp3:okhttp:4.12.0")
@file:DependsOn("com.google.code.gson:gson:2.10.1")

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson
import java.io.IOException
import java.security.MessageDigest
import java.util.*

// OAuth 服务器配置
const val BASE_URL = "http://localhost:8080"
const val APP_ID = "test_app_id"
const val APP_SECRET = "test_app_secret"
const val USER_ID = "test_user_123"

// HTTP 客户端
val httpClient = OkHttpClient()
val gson = Gson()

data class AuthCodeRequest(
    val appId: String,
    val userId: String,
    val scopes: String,
    val redirectUri: String?,
    val codeChallenge: String,
    val codeChallengeMethod: String,
    val state: String?
)

data class AuthCodeResponse(val code: String, val state: String?)

data class TokenRequest(
    val grantType: String,
    val code: String,
    val clientId: String,
    val clientSecret: String,
    val codeVerifier: String,
    val redirectUri: String?
)

data class TokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshToken: String,
    val scope: String,
    val openId: String,
    val unionId: String
)

data class UserInfoResponse(
    val openId: String,
    val unionId: String,
    val nickname: String,
    val avatar: String?,
    val userId: String,
    val isVerified: Boolean,
    val gender: String?,
    val location: String?
)

fun generatePKCE(): Pair<String, String> {
    val codeVerifier = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(ByteArray(32).also { Random().nextBytes(it) })
    
    val bytes = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray())
    val codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    
    return Pair(codeVerifier, codeChallenge)
}

fun main() {
    println("=== Android SDK 与 OAuth 服务器集成测试 ===\n")
    
    try {
        // 1. 测试健康检查
        println("1. 测试服务器健康状态...")
        testHealth()
        
        // 2. 生成 PKCE 参数
        val (codeVerifier, codeChallenge) = generatePKCE()
        println("2. 生成 PKCE 参数...")
        println("   Code Verifier: $codeVerifier")
        println("   Code Challenge: $codeChallenge\n")
        
        // 3. 获取授权码（模拟授权流程）
        println("3. 获取授权码...")
        val authCode = getAuthCode(codeChallenge)
        println("   授权码: $authCode\n")
        
        // 4. exchange 授权码换取访问令牌
        println("4. 换取访问令牌...")
        val tokenResponse = exchangeToken(authCode, codeVerifier)
        println("   访问令牌: ${tokenResponse.accessToken}")
        println("   刷新令牌: ${tokenResponse.refreshToken}\n")
        
        // 5. 获取用户信息
        println("5. 获取用户信息...")
        val userInfo = getUserInfo(tokenResponse.accessToken)
        println("   用户昵称: ${userInfo.nickname}")
        println("   用户ID: ${userInfo.userId}")
        println("   OpenID: ${userInfo.openId}\n")
        
        println("✅ 所有测试通过！Android SDK 与 OAuth 服务器连接正常")
        
    } catch (e: Exception) {
        println("❌ 测试失败: ${e.message}")
        e.printStackTrace()
    }
}

fun testHealth() {
    val request = Request.Builder()
        .url("$BASE_URL/health")
        .get()
        .build()
    
    val response = httpClient.newCall(request).execute()
    if (!response.isSuccessful) {
        throw Exception("服务器健康检查失败: ${response.code}")
    }
    println("   ✅ 服务器运行正常")
}

fun getAuthCode(codeChallenge: String): String {
    val authRequest = AuthCodeRequest(
        appId = APP_ID,
        userId = USER_ID,
        scopes = "basic_info,user_profile",
        redirectUri = "com.xiaohongshu.demo://oauth/callback",
        codeChallenge = codeChallenge,
        codeChallengeMethod = "S256",
        state = "test_state_android"
    )
    
    val requestBody = RequestBody.create(
        "application/json".toMediaType(),
        gson.toJson(authRequest)
    )
    
    val request = Request.Builder()
        .url("$BASE_URL/oauth/authorize")
        .post(requestBody)
        .build()
    
    val response = httpClient.newCall(request).execute()
    if (!response.isSuccessful) {
        throw Exception("获取授权码失败: ${response.code} - ${response.body?.string()}")
    }
    
    val authResponse = gson.fromJson(response.body?.string(), AuthCodeResponse::class.java)
    return authResponse.code
}

fun exchangeToken(authCode: String, codeVerifier: String): TokenResponse {
    val formBuilder = FormBody.Builder()
        .add("grant_type", "authorization_code")
        .add("code", authCode)
        .add("client_id", APP_ID)
        .add("client_secret", APP_SECRET)
        .add("code_verifier", codeVerifier)
        .add("redirect_uri", "com.xiaohongshu.demo://oauth/callback")
    
    val request = Request.Builder()
        .url("$BASE_URL/oauth/token")
        .post(formBuilder.build())
        .build()
    
    val response = httpClient.newCall(request).execute()
    if (!response.isSuccessful) {
        throw Exception("换取令牌失败: ${response.code} - ${response.body?.string()}")
    }
    
    return gson.fromJson(response.body?.string(), TokenResponse::class.java)
}

fun getUserInfo(accessToken: String): UserInfoResponse {
    val request = Request.Builder()
        .url("$BASE_URL/oauth/userinfo")
        .header("Authorization", "Bearer $accessToken")
        .get()
        .build()
    
    val response = httpClient.newCall(request).execute()
    if (!response.isSuccessful) {
        throw Exception("获取用户信息失败: ${response.code} - ${response.body?.string()}")
    }
    
    return gson.fromJson(response.body?.string(), UserInfoResponse::class.java)
}