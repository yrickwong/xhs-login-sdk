package com.xiaohongshu.login.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xiaohongshu.login.model.AuthResponse
import com.xiaohongshu.login.utils.AppUtils

class XHSEntryActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val response: AuthResponse? = intent?.getParcelableExtra(AppUtils.EXTRA_AUTH_RESPONSE)
        
        val resultIntent = Intent()
        if (response != null) {
            resultIntent.putExtra(AppUtils.EXTRA_AUTH_RESPONSE, response)
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        
        finish()
    }
}