package com.xiaohongshu.login.network

import com.xiaohongshu.login.model.XHSError

interface ApiCallback<T> {
    fun onSuccess(data: T)
    fun onError(error: XHSError)
}