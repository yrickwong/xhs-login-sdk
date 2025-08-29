package com.xiaohongshu.login.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xiaohongshu.login.XHSLoginManager
import com.xiaohongshu.login.callback.XHSLoginCallback
import com.xiaohongshu.login.callback.XHSUserCallback
import com.xiaohongshu.login.demo.databinding.ActivityMainBinding
import com.xiaohongshu.login.model.XHSError
import com.xiaohongshu.login.model.XHSScope
import com.xiaohongshu.login.model.XHSUser

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val APP_ID = "your_app_id_here"
        private const val APP_SECRET = "your_app_secret_here"
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var loginManager: XHSLoginManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initSDK()
        updateUI()
        setupClickListeners()
    }
    
    private fun initSDK() {
        loginManager = XHSLoginManager.getInstance()
        loginManager.configure(this, APP_ID, APP_SECRET)
    }
    
    private fun updateUI() {
        val isLoggedIn = loginManager.isLoggedIn()
        
        binding.btnLogin.visibility = if (isLoggedIn) View.GONE else View.VISIBLE
        binding.btnLogout.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.btnGetUserInfo.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.btnRefreshToken.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        
        if (isLoggedIn) {
            val userId = loginManager.getCachedUserId(this)
            val openId = loginManager.getCachedOpenId(this)
            binding.tvUserInfo.text = "User ID: $userId\nOpen ID: $openId"
        } else {
            binding.tvUserInfo.text = "未登录"
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener { login() }
        binding.btnLogout.setOnClickListener { logout() }
        binding.btnGetUserInfo.setOnClickListener { getUserInfo() }
        binding.btnRefreshToken.setOnClickListener { refreshToken() }
    }
    
    private fun login() {
        val scopes = arrayOf(XHSScope.BASIC_INFO, XHSScope.USER_PROFILE)
        
        loginManager.login(this, scopes, object : XHSLoginCallback {
            override fun onSuccess(user: XHSUser) {
                showToast("登录成功：${user.nickname}")
                updateUserInfo(user)
                updateUI()
            }
            
            override fun onError(error: XHSError) {
                showToast("登录失败：${error.message}")
            }
            
            override fun onCancel() {
                showToast("用户取消登录")
            }
        })
    }
    
    private fun logout() {
        loginManager.logout()
        showToast("已退出登录")
        updateUI()
    }
    
    private fun getUserInfo() {
        val accessToken = loginManager.getCachedAccessToken(this)
        if (accessToken == null) {
            showToast("请先登录")
            return
        }
        
        loginManager.getUserInfo(accessToken, object : XHSUserCallback {
            override fun onSuccess(user: XHSUser) {
                showToast("获取用户信息成功")
                updateUserInfo(user)
            }
            
            override fun onError(error: XHSError) {
                showToast("获取用户信息失败：${error.message}")
            }
        })
    }
    
    private fun refreshToken() {
        loginManager.refreshToken(object : XHSUserCallback {
            override fun onSuccess(user: XHSUser) {
                showToast("刷新Token成功")
                updateUserInfo(user)
            }
            
            override fun onError(error: XHSError) {
                showToast("刷新Token失败：${error.message}")
            }
        })
    }
    
    private fun updateUserInfo(user: XHSUser) {
        val info = buildString {
            append("昵称: ${user.nickname}\n")
            append("User ID: ${user.userId}\n")
            append("Open ID: ${user.openId}\n")
            append("Union ID: ${user.unionId}\n")
            append("头像: ${user.avatar}")
        }
        
        binding.tvUserInfo.text = info
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loginManager.handleActivityResult(requestCode, resultCode, data)
    }
}