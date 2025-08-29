# 小红书 OAuth 登录 SDK (Android)

小红书官方提供的 Android OAuth 登录 SDK，支持第三方应用通过 App-to-App 方式快速集成小红书登录功能。

**✨ 现已全面支持 Kotlin！享受更简洁、更安全的现代 Android 开发体验。**

## 授权流程

参考标准的 OAuth 2.0 授权码流程：

1. **第三方应用** 调用 SDK 发起小红书登录请求
2. **SDK** 构建授权请求，包含 PKCE 安全参数
3. **SDK** 通过 Intent 拉起小红书 App，请求用户授权
4. **小红书 App** 显示授权页面，用户确认授权
5. **小红书 App** 生成 authorization code，通过 Intent 返回给第三方应用
6. **SDK** 接收授权码，验证 PKCE 参数
7. **SDK** 使用授权码 + App ID + App Secret 换取 access_token
8. **SDK** 获取用户信息，通过回调返回给第三方应用

## 功能特性

- **App-to-App 授权**：直接拉起小红书 App 进行用户授权，用户体验流畅
- **标准 OAuth 2.0**：遵循 OAuth 2.0 Authorization Code 流程
- **PKCE 安全保障**：集成 PKCE (Proof Key for Code Exchange) 安全机制，防止授权码拦截攻击
- **简单易用**：提供简洁的 API 接口，几行代码即可完成集成
- **数据加密**：Token 信息本地加密存储，保护用户隐私
- **自动刷新**：支持 Access Token 自动刷新机制
- **错误处理**：完善的错误处理和回调机制

## 系统要求

- Android API Level 21 (Android 5.0) 及以上
- 已安装小红书 App (com.xingin.xhs)

## 快速开始

### 1. 添加依赖

在项目的 `build.gradle` 文件中添加依赖：

```gradle
dependencies {
    implementation project(':xhslogin')  // 本地依赖
    // 或者使用远程依赖 (发布后可用)
    // implementation 'com.xiaohongshu:xhs-login-sdk:1.0.0'
}
```

### 2. 配置权限

在 `AndroidManifest.xml` 中添加必要权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 检测小红书 App 是否安装 -->
<queries>
    <package android:name="com.xingin.xhs" />
</queries>
```

### 3. 配置回调入口 Activity

在 `AndroidManifest.xml` 中配置小红书回调入口 Activity：

```xml
<!-- 小红书回调入口 Activity -->
<activity
    android:name=".XHSEntryActivity"
    android:exported="true"
    android:launchMode="singleTop"
    android:taskAffinity="${applicationId}.diff"
    android:theme="@android:style/Theme.Translucent.NoTitleBar" />
```

创建 `XHSEntryActivity.kt` 或 `XHSEntryActivity.java`：

**Kotlin:**
```kotlin
class XHSEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val response: AuthResponse? = intent?.getParcelableExtra(AppUtils.EXTRA_AUTH_RESPONSE)
        
        val resultIntent = Intent()
        if (response != null) {
            resultIntent.putExtra(AppUtils.EXTRA_AUTH_RESPONSE, response)
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        
        finish()
    }
}
```

**Java:**
```java
public class XHSEntryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        if (intent != null) {
            AuthResponse response = intent.getParcelableExtra(AppUtils.EXTRA_AUTH_RESPONSE);
            if (response != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppUtils.EXTRA_AUTH_RESPONSE, response);
                setResult(Activity.RESULT_OK, resultIntent);
            } else {
                setResult(Activity.RESULT_CANCELED);
            }
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        
        finish();
    }
}
```

### 4. 初始化 SDK

在 Application 或主 Activity 中初始化：

**Kotlin:**
```kotlin
val loginManager = XHSLoginManager.getInstance()
loginManager.configure(this, APP_ID, APP_SECRET)
```

**Java:**
```java
XHSLoginManager loginManager = XHSLoginManager.getInstance();
loginManager.configure(this, APP_ID, APP_SECRET);
```

### 5. 发起登录

**Kotlin:**
```kotlin
val scopes = arrayOf(XHSScope.BASIC_INFO, XHSScope.USER_PROFILE)

loginManager.login(this, scopes, object : XHSLoginCallback {
    override fun onSuccess(user: XHSUser) {
        // 登录成功，获取用户信息
        Log.d(TAG, "用户昵称: ${user.nickname}")
        Log.d(TAG, "用户ID: ${user.userId}")
        Log.d(TAG, "Access Token: ${user.accessToken}")
    }
    
    override fun onError(error: XHSError) {
        // 登录失败
        Log.e(TAG, "登录失败: ${error.message}")
    }
    
    override fun onCancel() {
        // 用户取消登录
        Log.d(TAG, "用户取消登录")
    }
})
```

**Java:**
```java
String[] scopes = {XHSScope.BASIC_INFO, XHSScope.USER_PROFILE};

loginManager.login(this, scopes, new XHSLoginCallback() {
    @Override
    public void onSuccess(XHSUser user) {
        // 登录成功，获取用户信息
        Log.d(TAG, "用户昵称: " + user.getNickname());
        Log.d(TAG, "用户ID: " + user.getUserId());
        Log.d(TAG, "Access Token: " + user.getAccessToken());
    }
    
    @Override
    public void onError(XHSError error) {
        // 登录失败
        Log.e(TAG, "登录失败: " + error.getMessage());
    }
    
    @Override
    public void onCancel() {
        // 用户取消登录
        Log.d(TAG, "用户取消登录");
    }
});
```

### 6. 处理 Activity 结果

在主 Activity 的 `onActivityResult` 方法中：

**Kotlin:**
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    loginManager.handleActivityResult(requestCode, resultCode, data)
}
```

**Java:**
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    loginManager.handleActivityResult(requestCode, resultCode, data);
}
```

## 详细 API 说明

### XHSLoginManager

主要的 SDK 入口类，提供所有登录相关功能。

#### 方法说明

**Kotlin:**
```kotlin
// 配置 SDK
fun configure(context: Context, appId: String, appSecret: String)

// 登录（使用默认权限范围）
fun login(activity: Activity, callback: XHSLoginCallback)

// 登录（指定权限范围）
fun login(activity: Activity, scopes: Array<String>?, callback: XHSLoginCallback)

// 获取用户信息
fun getUserInfo(accessToken: String, callback: XHSUserCallback)

// 刷新 Token
fun refreshToken(callback: XHSUserCallback)

// 退出登录
fun logout()

// 检查登录状态
fun isLoggedIn(): Boolean

// 获取缓存的 Access Token
fun getCachedAccessToken(context: Context): String?

// 处理授权结果
fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
```

**Java:**
```java
// 配置 SDK
public void configure(Context context, String appId, String appSecret)

// 登录（使用默认权限范围）
public void login(Activity activity, XHSLoginCallback callback)

// 登录（指定权限范围）
public void login(Activity activity, String[] scopes, XHSLoginCallback callback)

// 获取用户信息
public void getUserInfo(String accessToken, XHSUserCallback callback)

// 刷新 Token
public void refreshToken(XHSUserCallback callback)

// 退出登录
public void logout()

// 检查登录状态
public boolean isLoggedIn()

// 获取缓存的 Access Token
public String getCachedAccessToken(Context context)

// 处理授权结果
public void handleActivityResult(int requestCode, int resultCode, Intent data)
```

### XHSUser

用户信息模型类。

**Kotlin:**
```kotlin
data class XHSUser(
    var userId: String? = null,        // 用户ID
    var openId: String? = null,        // Open ID
    var nickname: String? = null,      // 昵称
    var avatar: String? = null,        // 头像URL
    var unionId: String? = null,       // Union ID
    var accessToken: String? = null,   // Access Token
    var refreshToken: String? = null,  // Refresh Token
    var expiresIn: Long = 0,           // Token过期时间
    var scopes: Array<String>? = null  // 权限范围
)
```

**Java:**
```java
public class XHSUser {
    private String userId;        // 用户ID
    private String openId;        // Open ID
    private String nickname;      // 昵称
    private String avatar;        // 头像URL
    private String unionId;       // Union ID
    private String accessToken;   // Access Token
    private String refreshToken;  // Refresh Token
    private long expiresIn;       // Token过期时间
    private String[] scopes;      // 权限范围
}
```

### XHSScope

权限范围常量定义。

**Kotlin:**
```kotlin
object XHSScope {
    const val BASIC_INFO = "basic_info"          // 基本信息
    const val USER_PROFILE = "user_profile"      // 用户资料
    const val READ_NOTES = "read_notes"          // 读取笔记
    const val WRITE_NOTES = "write_notes"        // 发布笔记
    const val READ_FOLLOWERS = "read_followers"  // 读取粉丝
    
    @JvmStatic
    fun getDefaultScopes(): Array<String> = arrayOf(BASIC_INFO, USER_PROFILE)
}
```

**Java:**
```java
public class XHSScope {
    public static final String BASIC_INFO = "basic_info";          // 基本信息
    public static final String USER_PROFILE = "user_profile";      // 用户资料
    public static final String READ_NOTES = "read_notes";          // 读取笔记
    public static final String WRITE_NOTES = "write_notes";        // 发布笔记
    public static final String READ_FOLLOWERS = "read_followers";  // 读取粉丝
}
```

### XHSError

错误信息模型类。

```java
public class XHSError {
    public static final int ERROR_NETWORK = 1001;           // 网络错误
    public static final int ERROR_INVALID_PARAMS = 1002;    // 参数错误
    public static final int ERROR_AUTH_FAILED = 1003;       // 授权失败
    public static final int ERROR_TOKEN_EXPIRED = 1004;     // Token过期
    public static final int ERROR_APP_NOT_INSTALLED = 1005; // App未安装
    public static final int ERROR_UNKNOWN = 1999;           // 未知错误
}
```

## 回调接口

### XHSLoginCallback

登录回调接口：

```java
public interface XHSLoginCallback {
    void onSuccess(XHSUser user);  // 登录成功
    void onError(XHSError error);  // 登录失败
    void onCancel();               // 用户取消
}
```

### XHSUserCallback

用户信息获取回调接口：

```java
public interface XHSUserCallback {
    void onSuccess(XHSUser user);  // 成功获取用户信息
    void onError(XHSError error);  // 获取失败
}
```

## 最佳实践

### 1. 错误处理

```java
@Override
public void onError(XHSError error) {
    switch (error.getCode()) {
        case XHSError.ERROR_APP_NOT_INSTALLED:
            // 提示用户下载安装小红书 App
            showInstallDialog();
            break;
        case XHSError.ERROR_NETWORK:
            // 网络错误，提示重试
            showRetryDialog();
            break;
        case XHSError.ERROR_TOKEN_EXPIRED:
            // Token 过期，尝试刷新
            loginManager.refreshToken(callback);
            break;
        default:
            showErrorMessage(error.getMessage());
            break;
    }
}
```

### 2. Token 管理

```java
// 检查 Token 是否有效
if (loginManager.isLoggedIn()) {
    String accessToken = loginManager.getCachedAccessToken(this);
    // 使用 Token 调用 API
} else {
    // 需要重新登录
    startLogin();
}
```

### 3. 生命周期管理

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    // SDK 会自动处理生命周期，无需手动清理
}
```

## 安全说明

- SDK 使用 PKCE (Proof Key for Code Exchange) 机制防止授权码拦截
- 所有敏感数据（如 Access Token）在本地加密存储
- 网络传输使用 HTTPS 协议
- 建议在生产环境中使用签名验证

## 常见问题

### Q: 提示"小红书 App 未安装"怎么办？
A: 用户需要先安装小红书 App。可以引导用户到应用商店下载。

### Q: 授权后没有回调怎么办？
A: 检查 AndroidManifest.xml 中的 XHSEntryActivity 配置是否正确，确保 exported="true" 并且 taskAffinity 配置正确。

### Q: Token 过期了怎么办？
A: 使用 `refreshToken()` 方法刷新 Token，或者重新发起登录。

### Q: 如何处理网络异常？
A: SDK 内置了网络重试机制，建议在回调中根据错误码进行相应处理。

## 示例项目

完整的示例项目请查看 `demo` 目录，包含了所有功能的完整实现。

## 版本历史

### v1.0.0 (2023-12-01)
- 初始版本发布
- 支持 App-to-App 授权流程
- 集成 PKCE 安全机制
- 提供完整的用户信息获取功能

## 技术支持

如有问题请联系小红书开发者支持团队或在 GitHub 提交 Issue。

## 许可证

本项目采用 MIT 许可证，详情请参阅 LICENSE 文件。