package com.xingin.xhs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xingin.xhs.databinding.ActivityMainBinding

/**
 * 模拟小红书App主Activity
 * 
 * 这个Activity模拟小红书App的主界面
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.tvWelcome.text = "欢迎使用小红书 (模拟版)\n\n这是一个模拟的小红书App，用于测试OAuth SDK的App-to-App授权流程。"
        binding.tvInfo.text = "当第三方应用请求授权时，会自动启动OAuth授权界面。"
    }
}