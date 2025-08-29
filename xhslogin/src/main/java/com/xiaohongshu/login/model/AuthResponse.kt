package com.xiaohongshu.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthResponse(
    var errCode: Int = 0,
    var errStr: String? = null,
    var code: String? = null,
    var state: String? = null,
) : Parcelable {

    companion object {
        const val ERR_OK = 0
        const val ERR_USER_CANCEL = -2
        const val ERR_AUTH_DENIED = -4
        const val ERR_UNSUPPORTED = -5
    }

    val isSuccessful: Boolean
        get() = errCode == ERR_OK
}
