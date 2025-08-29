package com.xiaohongshu.login.callback

import com.xiaohongshu.login.model.XHSUser
import com.xiaohongshu.login.model.XHSError

interface XHSUserCallback {
    fun onSuccess(user: XHSUser)
    fun onError(error: XHSError)
}