package com.xiaohongshu.login.model

data class XHSUser(
    var userId: String? = null,
    var openId: String? = null,
    var nickname: String? = null,
    var avatar: String? = null,
    var unionId: String? = null,
    var accessToken: String? = null,
    var refreshToken: String? = null,
    var expiresIn: Long = 0,
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
