package com.xiaohongshu.login.network

import com.xiaohongshu.login.model.XHSError

/**
 * API 网络请求回调接口
 * 
 * 用于处理异步网络请求的结果回调。
 * 提供统一的成功和错误处理机制。
 * 
 * @param T 成功响应的数据类型
 */
interface ApiCallback<T> {
    
    /**
     * 网络请求成功回调
     * 
     * 当网络请求成功并且响应数据解析完成时调用。
     * 此方法在主线程中执行，可以直接更新 UI。
     * 
     * @param data 解析后的响应数据
     */
    fun onSuccess(data: T)
    
    /**
     * 网络请求错误回调
     * 
     * 当网络请求失败、服务器返回错误或数据解析失败时调用。
     * 此方法在主线程中执行，可以直接更新 UI。
     * 
     * @param error 包含错误码和错误信息的错误对象
     */
    fun onError(error: XHSError)
}
