package com.xiaohongshu.login.callback

import com.xiaohongshu.login.model.XHSError
import com.xiaohongshu.login.model.XHSUser

/**
 * 用户信息操作回调接口
 * 
 * 用于处理用户信息相关操作的结果回调，如获取用户信息、刷新令牌等。
 * 与登录回调不同的是，此接口不包含取消状态，因为这些操作通常是自动进行的。
 */
interface XHSUserCallback {
    
    /**
     * 操作成功回调
     * 
     * 当用户信息获取或令牌刷新成功时调用。
     * 返回的用户对象包含最新的用户信息和令牌数据。
     * 
     * @param user 包含最新用户信息和访问令牌的用户对象
     */
    fun onSuccess(user: XHSUser)
    
    /**
     * 操作失败回调
     * 
     * 当用户信息获取或令牌刷新失败时调用。
     * 可能的失败原因包括网络错误、令牌过期、权限不足等。
     * 
     * @param error 包含错误码和错误信息的错误对象
     */
    fun onError(error: XHSError)
}
