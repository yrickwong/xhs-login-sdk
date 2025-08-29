package com.xiaohongshu.login.model

/**
 * 小红书 OAuth 权限范围定义
 * 
 * 定义了所有可用的权限范围常量，用于指定应用需要请求的用户权限。
 * 不同的权限范围允许应用访问用户不同类型的数据和执行不同的操作。
 */
object XHSScope {
    /** 基本信息权限 - 获取用户的基本公开信息 */
    const val BASIC_INFO = "basic_info"
    
    /** 用户档案权限 - 获取用户的详细档案信息 */
    const val USER_PROFILE = "user_profile"
    
    /** 读取笔记权限 - 获取用户发布的笔记内容 */
    const val READ_NOTES = "read_notes"
    
    /** 写入笔记权限 - 代表用户发布或修改笔记 */
    const val WRITE_NOTES = "write_notes"
    
    /** 读取关注者权限 - 获取用户的关注者信息 */
    const val READ_FOLLOWERS = "read_followers"

    /**
     * 获取默认权限范围
     * 
     * 返回最基本的权限范围组合，适用于大部分基础使用场景。
     * 包括基本信息和用户档案权限。
     * 
     * @return 默认权限范围数组
     */
    @JvmStatic
    fun getDefaultScopes(): Array<String> {
        return arrayOf(BASIC_INFO, USER_PROFILE)
    }
}
