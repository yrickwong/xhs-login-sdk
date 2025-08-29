package com.xiaohongshu.login.model

object XHSScope {
    const val BASIC_INFO = "basic_info"
    const val USER_PROFILE = "user_profile"
    const val READ_NOTES = "read_notes"
    const val WRITE_NOTES = "write_notes"
    const val READ_FOLLOWERS = "read_followers"
    
    @JvmStatic
    fun getDefaultScopes(): Array<String> {
        return arrayOf(BASIC_INFO, USER_PROFILE)
    }
}