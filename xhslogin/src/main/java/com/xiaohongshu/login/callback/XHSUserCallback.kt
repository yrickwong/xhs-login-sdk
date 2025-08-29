package com.xiaohongshu.login.callback

import com.xiaohongshu.login.model.XHSError
import com.xiaohongshu.login.model.XHSUser

interface XHSUserCallback {
    fun onSuccess(user: XHSUser)
    fun onError(error: XHSError)
}
