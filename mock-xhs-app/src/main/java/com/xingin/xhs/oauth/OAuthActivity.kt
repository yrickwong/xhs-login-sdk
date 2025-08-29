package com.xingin.xhs.oauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.xingin.xhs.R
import com.xingin.xhs.databinding.ActivityOauthBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException

/**
 * 模拟小红书OAuth授权Activity
 * 
 * 这个Activity模拟小红书App中的OAuth授权流程：
 * 1. 接收来自第三方APP的授权请求
 * 2. 显示授权界面给用户确认
 * 3. 用户确认后，与OAuth服务器交互获取授权码
 * 4. 将授权码返回给第三方APP
 */
class OAuthActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "XHS_OAuthActivity"
        private const val OAUTH_SERVER_URL = "http://10.0.2.2:8080" // 模拟器访问本机localhost
        private const val TEST_USER_ID = "test_user_123"
    }
    
    private lateinit var binding: ActivityOauthBinding
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    
    // 从Intent中解析的授权请求参数
    private var authRequest: AuthRequest? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOauthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        parseAuthRequest()
        setupUI()
        setupClickListeners()
    }
    
    /**
     * 解析来自第三方APP的授权请求
     */
    private fun parseAuthRequest() {
        try {
            val appId = intent.getStringExtra("app_id")
            val scope = intent.getStringArrayExtra("scope")
            val state = intent.getStringExtra("state")
            val codeChallenge = intent.getStringExtra("code_challenge")
            val codeChallengeMethod = intent.getStringExtra("code_challenge_method")
            
            if (appId.isNullOrBlank()) {
                val errorMsg = "无效的授权请求：缺少应用ID"
                Log.e(TAG, errorMsg)
                showToast(errorMsg)
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }
            
            authRequest = AuthRequest(
                appId = appId,
                scope = scope,
                state = state,
                codeChallenge = codeChallenge,
                codeChallengeMethod = codeChallengeMethod
            )
        } catch (e: Exception) {
            val errorMsg = "解析授权请求失败: ${e.message}"
            Log.e(TAG, errorMsg, e)
            showToast(errorMsg)
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    
    /**
     * 设置UI显示
     */
    private fun setupUI() {
        authRequest?.let { request ->
            binding.tvAppId.text = "应用ID: ${request.appId}"
            binding.tvScopes.text = "权限范围: ${request.scope?.joinToString(", ")}"
            binding.tvState.text = "状态值: ${request.state}"
            binding.tvCodeChallenge.text = "PKCE挑战: ${request.codeChallenge?.take(20)}..."
        }
    }
    
    /**
     * 设置按钮点击事件
     */
    private fun setupClickListeners() {
        binding.btnAuthorize.setOnClickListener {
            authorizeApp()
        }
        
        binding.btnDeny.setOnClickListener {
            denyAuthorization()
        }
        
        binding.btnCancel.setOnClickListener {
            cancelAuthorization()
        }
    }
    
    /**
     * 用户授权 - 与OAuth服务器交互获取授权码
     */
    private fun authorizeApp() {
        binding.btnAuthorize.isEnabled = false
        binding.btnAuthorize.text = "授权中..."
        
        authRequest?.let { request ->
            // 构建授权请求URL
            val url = HttpUrl.Builder()
                .scheme("http")
                .host("10.0.2.2")
                .port(8080)
                .addPathSegments("oauth/authorize")
                .addQueryParameter("client_id", request.appId)
                .addQueryParameter("scope", request.scope?.joinToString(","))
                .addQueryParameter("code_challenge", request.codeChallenge)
                .addQueryParameter("code_challenge_method", request.codeChallengeMethod)
                .addQueryParameter("state", request.state)
                .build()
                
            Log.d(TAG, "Making OAuth request to: $url")
            
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            
            // 异步请求OAuth服务器
            httpClient.newCall(httpRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        val errorMsg = "网络请求失败: ${e.message}"
                        Log.e(TAG, errorMsg, e)
                        showToast(errorMsg)
                        resetAuthButton()
                    }
                }
                
                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        handleAuthServerResponse(response)
                    }
                }
            })
        }
    }
    
    /**
     * 处理OAuth服务器的响应
     */
    private fun handleAuthServerResponse(response: Response) {
        try {
            if (response.isSuccessful) {
                // 检查是否是重定向响应
                val location = response.header("Location")
                if (location != null) {
                    // 从重定向URL中提取授权码
                    val uri = android.net.Uri.parse(location)
                    val code = uri.getQueryParameter("code")
                    val state = uri.getQueryParameter("state")
                    
                    if (!code.isNullOrBlank()) {
                        returnSuccessResult(code, state)
                        return
                    }
                }
                
                // 如果不是重定向，尝试解析JSON响应
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrBlank()) {
                    val authCodeResponse = gson.fromJson(responseBody, AuthCodeResponse::class.java)
                    if (!authCodeResponse.code.isNullOrBlank()) {
                        returnSuccessResult(authCodeResponse.code, authCodeResponse.state)
                        return
                    }
                }
            }
            
            // 如果到这里说明授权失败
            val errorBody = response.body?.string() ?: "未知错误"
            val errorMsg = "授权失败: $errorBody"
            Log.e(TAG, "$errorMsg, response code: ${response.code}")
            showToast(errorMsg)
            resetAuthButton()
            
        } catch (e: Exception) {
            val errorMsg = "处理服务器响应失败: ${e.message}"
            Log.e(TAG, errorMsg, e)
            showToast(errorMsg)
            resetAuthButton()
        } finally {
            response.close()
        }
    }
    
    /**
     * 返回成功的授权结果
     */
    private fun returnSuccessResult(code: String, state: String?) {
        Log.d(TAG, "Returning success result: code=$code, state=$state")
        val response = AuthResponse(
            errCode = AuthResponse.ERR_OK,
            code = code,
            state = state
        )
        
        val resultIntent = Intent().apply {
            putExtra("err_code", response.errCode)
            putExtra("err_str", response.errStr)
            putExtra("code", response.code)
            putExtra("state", response.state)
        }
        
        setResult(Activity.RESULT_OK, resultIntent)
        showToast("授权成功！")
        finish()
    }
    
    /**
     * 用户拒绝授权
     */
    private fun denyAuthorization() {
        val response = AuthResponse(
            errCode = AuthResponse.ERR_AUTH_DENIED,
            errStr = "用户拒绝授权"
        )
        
        val resultIntent = Intent().apply {
            putExtra("err_code", response.errCode)
            putExtra("err_str", response.errStr)
            putExtra("code", response.code)
            putExtra("state", response.state)
        }
        
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    
    /**
     * 用户取消授权
     */
    private fun cancelAuthorization() {
        val response = AuthResponse(
            errCode = AuthResponse.ERR_USER_CANCEL,
            errStr = "用户取消授权"
        )
        
        val resultIntent = Intent().apply {
            putExtra("err_code", response.errCode)
            putExtra("err_str", response.errStr)
            putExtra("code", response.code)
            putExtra("state", response.state)
        }
        
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    
    private fun resetAuthButton() {
        binding.btnAuthorize.isEnabled = true
        binding.btnAuthorize.text = "授权"
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

/**
 * 授权请求数据类（复制自SDK，避免依赖冲突）
 */
data class AuthRequest(
    var appId: String? = null,
    var scope: Array<String>? = null,
    var state: String? = null,
    var codeChallenge: String? = null,
    var codeChallengeMethod: String? = null
) : android.os.Parcelable {
    constructor(parcel: android.os.Parcel) : this(
        parcel.readString(),
        parcel.createStringArray(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeString(appId)
        parcel.writeStringArray(scope)
        parcel.writeString(state)
        parcel.writeString(codeChallenge)
        parcel.writeString(codeChallengeMethod)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<AuthRequest> {
        override fun createFromParcel(parcel: android.os.Parcel): AuthRequest {
            return AuthRequest(parcel)
        }

        override fun newArray(size: Int): Array<AuthRequest?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * 授权响应数据类（复制自SDK，避免依赖冲突）
 */
data class AuthResponse(
    var errCode: Int = 0,
    var errStr: String? = null,
    var code: String? = null,
    var state: String? = null
) : android.os.Parcelable {
    companion object {
        const val ERR_OK = 0
        const val ERR_USER_CANCEL = -2
        const val ERR_AUTH_DENIED = -4
        const val ERR_UNSUPPORTED = -5
        
        @JvmField
        val CREATOR = object : android.os.Parcelable.Creator<AuthResponse> {
            override fun createFromParcel(parcel: android.os.Parcel): AuthResponse {
                return AuthResponse(parcel)
            }
            override fun newArray(size: Int): Array<AuthResponse?> = arrayOfNulls(size)
        }
    }
    
    constructor(parcel: android.os.Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeInt(errCode)
        parcel.writeString(errStr)
        parcel.writeString(code)
        parcel.writeString(state)
    }

    override fun describeContents(): Int = 0
}

/**
 * 授权码响应数据类（用于解析OAuth服务器JSON响应）
 */
data class AuthCodeResponse(
    val code: String,
    val state: String?
)