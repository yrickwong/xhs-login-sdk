package com.xiaohongshu.login.callback

import com.xiaohongshu.login.model.XHSError
import com.xiaohongshu.login.model.XHSUser

/**
 * 小红书登录回调接口
 * 
 * 用于处理登录授权流程的结果回调。
 * 提供成功、失败和用户取消三种状态的处理。
 */
interface XHSLoginCallback {
    
    /**
     * 登录成功回调
     * 
     * 当用户完成授权并成功获取到用户信息时调用。
     * 此时可以进行 UI 跳转或数据更新等操作。
     * 
     * @param user 包含用户信息和访问令牌的用户对象
     */
    fun onSuccess(user: XHSUser)
    
    /**
     * 登录失败回调
     * 
     * 当登录过程中发生错误时调用，包括网络错误、授权失败等情况。
     * 可以根据错误码进行不同的处理或提示用户。
     * 
     * @param error 包含错误码和错误信息的错误对象
     */
    fun onError(error: XHSError)
    
    /**
     * 用户取消登录回调
     * 
     * 当用户在授权过程中主动取消时调用。
     * 通常可以关闭登录界面或提示用户重新尝试。
     */
    fun onCancel()
}
