package com.xiaohongshu.login.model

/**
 * 小红书用户信息数据模型
 * 
 * 包含用户的基本信息和授权令牌信息。
 * 在成功完成 OAuth 授权流程后返回给第三方应用。
 */
data class XHSUser(
    /** 用户在小红书平台的唯一标识符 */
    var userId: String? = null,
    
    /** 用户在当前应用中的唯一标识符，用于标识用户身份 */
    var openId: String? = null,
    
    /** 用户昵称 */
    var nickname: String? = null,
    
    /** 用户头像 URL */
    var avatar: String? = null,
    
    /** 用户在开发者账号下的统一标识符，可用于关联多个应用下的同一用户 */
    var unionId: String? = null,
    
    /** OAuth 访问令牌，用于调用小红书 API */
    var accessToken: String? = null,
    
    /** OAuth 刷新令牌，用于获取新的访问令牌 */
    var refreshToken: String? = null,
    
    /** 访问令牌的过期时间（Unix 时间戳，秒） */
    var expiresIn: Long = 0,
    
    /** 用户授权的权限范围数组 */
    var scopes: Array<String>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as XHSUser

        if (userId != other.userId) return false
        if (openId != other.openId) return false
        if (nickname != other.nickname) return false
        if (avatar != other.avatar) return false
        if (unionId != other.unionId) return false
        if (accessToken != other.accessToken) return false
        if (refreshToken != other.refreshToken) return false
        if (expiresIn != other.expiresIn) return false
        if (scopes != null) {
            if (other.scopes == null) return false
            if (!scopes.contentEquals(other.scopes)) return false
        } else if (other.scopes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId?.hashCode() ?: 0
        result = 31 * result + (openId?.hashCode() ?: 0)
        result = 31 * result + (nickname?.hashCode() ?: 0)
        result = 31 * result + (avatar?.hashCode() ?: 0)
        result = 31 * result + (unionId?.hashCode() ?: 0)
        result = 31 * result + (accessToken?.hashCode() ?: 0)
        result = 31 * result + (refreshToken?.hashCode() ?: 0)
        result = 31 * result + expiresIn.hashCode()
        result = 31 * result + (scopes?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "XHSUser(userId='$userId', openId='$openId', nickname='$nickname', avatar='$avatar', unionId='$unionId', expiresIn=$expiresIn)"
    }
}
