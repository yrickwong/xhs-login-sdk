package com.xiaohongshu.login.callback

import com.xiaohongshu.login.model.XHSUser
import com.xiaohongshu.login.model.XHSError

interface XHSLoginCallback {
    fun onSuccess(user: XHSUser)
    fun onError(error: XHSError)
    fun onCancel()
}