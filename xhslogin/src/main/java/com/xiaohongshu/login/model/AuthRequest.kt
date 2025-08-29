package com.xiaohongshu.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthRequest(
    var appId: String? = null,
    var scope: Array<String>? = null,
    var state: String? = null,
    var codeChallenge: String? = null,
    var codeChallengeMethod: String? = null
) : Parcelable {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuthRequest

        if (appId != other.appId) return false
        if (scope != null) {
            if (other.scope == null) return false
            if (!scope.contentEquals(other.scope)) return false
        } else if (other.scope != null) return false
        if (state != other.state) return false
        if (codeChallenge != other.codeChallenge) return false
        if (codeChallengeMethod != other.codeChallengeMethod) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appId?.hashCode() ?: 0
        result = 31 * result + (scope?.contentHashCode() ?: 0)
        result = 31 * result + (state?.hashCode() ?: 0)
        result = 31 * result + (codeChallenge?.hashCode() ?: 0)
        result = 31 * result + (codeChallengeMethod?.hashCode() ?: 0)
        return result
    }
}