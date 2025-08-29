# 小红书 OAuth Login SDK 交互流程图

## 1. 整体架构图

![整体架构图](diagrams/01-architecture.png)

```mermaid
graph TB
    subgraph "第三方应用"
        A[第三方App] --> B[XHSLoginManager]
        B --> C[AuthManager]
        C --> D[XHSEntryActivity]
    end
    
    subgraph "小红书应用"
        E[小红书App] --> F[OAuthActivity]
    end
    
    subgraph "小红书服务器"
        G[OAuth授权服务器] --> H[用户信息API]
    end
    
    subgraph "SDK核心组件"
        I[PKCEHelper] --> J[CryptoUtils]
        K[ApiClient] --> L[StorageUtils]
    end
    
    A -.-> E
    D -.-> F
    C --> K
    C --> I
    K --> G
```

## 2. 完整OAuth授权流程

![OAuth授权流程](diagrams/02-oauth-flow.png)

```mermaid
sequenceDiagram
    participant App as 第三方App
    participant SDK as XHSLoginManager
    participant Auth as AuthManager  
    participant PKCE as PKCEHelper
    participant XHS as 小红书App
    participant Entry as XHSEntryActivity
    participant Server as OAuth服务器
    participant Storage as StorageUtils

    Note over App,Storage: 1. 初始化阶段
    App->>+SDK: configure(context, appId, appSecret)
    SDK->>+Auth: 创建AuthManager实例
    Auth-->>-SDK: 初始化完成
    SDK-->>-App: 配置完成

    Note over App,Storage: 2. 登录流程启动
    App->>+SDK: login(activity, scopes, callback)
    SDK->>+Auth: login(activity, scopes, callback)
    Auth->>+PKCE: 生成PKCE参数
    PKCE->>PKCE: 生成code_verifier
    PKCE->>PKCE: 生成code_challenge
    PKCE->>PKCE: 生成state参数
    PKCE-->>-Auth: 返回PKCE参数

    Auth->>Auth: 创建AuthRequest
    Note right of Auth: AuthRequest包含:\n- appId\n- scopes\n- state\n- code_challenge\n- code_challenge_method

    Auth->>+XHS: startActivityForResult(Intent)
    Note right of Auth: Intent目标:\ncom.xingin.xhs/.oauth.OAuthActivity

    Note over App,Storage: 3. 小红书App处理
    XHS->>XHS: 显示授权页面
    XHS->>XHS: 用户确认/拒绝授权
    XHS->>+Entry: 返回AuthResponse
    
    Note over App,Storage: 4. 授权结果处理
    Entry->>Entry: 处理AuthResponse
    Entry-->>-Auth: onActivityResult()
    
    Auth->>Auth: 验证state参数
    Auth->>PKCE: verifyState(state)
    PKCE-->>Auth: 验证结果

    alt 授权成功
        Auth->>+Server: 交换authorization_code
        Note right of Auth: TokenRequest包含:\n- code\n- client_id\n- client_secret\n- code_verifier
        
        Server->>Server: 验证code_verifier
        Server-->>-Auth: 返回access_token+refresh_token
        
        Auth->>+Storage: 保存token信息
        Storage->>Storage: AES加密存储
        Storage-->>-Auth: 保存完成
        
        Auth->>+Server: 获取用户信息
        Server-->>-Auth: 返回用户信息
        
        Auth->>+Storage: 保存用户信息
        Storage-->>-Auth: 保存完成
        
        Auth-->>SDK: onSuccess(XHSUser)
        SDK-->>App: onSuccess(XHSUser)
    else 授权失败/取消
        Auth-->>SDK: onError(XHSError) 或 onCancel()
        SDK-->>App: onError(XHSError) 或 onCancel()
    end
```

## 3. 核心组件交互图

![核心组件交互图](diagrams/03-components.png)

```mermaid
graph LR
    subgraph "公共API层"
        A["XHSLoginManager\n单例模式"]
    end
    
    subgraph "核心业务层"
        B["AuthManager\n授权管理"]
        C["PKCEHelper\n安全增强"]
    end
    
    subgraph "网络层"
        D["ApiClient\nHTTP客户端"]
        E["AuthService\n接口定义"]
    end
    
    subgraph "安全层"
        F["CryptoUtils\n加密工具"]
        G["SignatureVerifier\n签名验证"]
    end
    
    subgraph "存储层"
        H["StorageUtils\n本地存储"]
    end
    
    subgraph "工具层"
        I["AppUtils\n应用检测"]
    end
    
    A --> B
    B --> C
    B --> D
    B --> I
    D --> E
    B --> H
    H --> F
    B --> G
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style C fill:#fff3e0
    style D fill:#e8f5e8
    style E fill:#e8f5e8
    style F fill:#fce4ec
    style G fill:#fce4ec
    style H fill:#fff8e1
    style I fill:#f1f8e9
```

## 4. App-to-App通信流程

![App-to-App通信流程](diagrams/04-app2app.png)

```mermaid
sequenceDiagram
    participant ThirdParty as 第三方App
    participant Intent as Android Intent
    participant XHSApp as 小红书App
    participant Entry as XHSEntryActivity

    Note over ThirdParty,Entry: App-to-App授权通信
    
    ThirdParty->>+Intent: 创建Intent
    Note right of Intent: ComponentName:\ncom.xingin.xhs/\n.oauth.OAuthActivity
    
    Intent->>Intent: 添加AuthRequest数据
    Note right of Intent: Extra:\nEXTRA_AUTH_REQUEST
    
    ThirdParty->>+XHSApp: startActivityForResult()
    
    XHSApp->>XHSApp: 显示OAuth授权页面
    XHSApp->>XHSApp: 处理用户授权决定
    
    XHSApp->>+Entry: 返回结果Intent
    Note right of Entry: Extra:\nEXTRA_AUTH_RESPONSE\n包含AuthResponse对象
    
    Entry->>Entry: 处理AuthResponse
    Entry-->>-ThirdParty: setResult() + finish()
    
    ThirdParty->>ThirdParty: onActivityResult()
    Note right of ThirdParty: 解析AuthResponse\n继续后续流程
```

## 5. 安全机制流程图

![安全机制流程图](diagrams/05-security.png)

```mermaid
graph TD
    A[开始登录] --> B[生成PKCE参数]
    B --> C["code_verifier\n随机字符串"]
    B --> D["code_challenge\nSHA256(code_verifier)"]
    B --> E["state\nCSRF防护"]
    
    C --> F[本地临时存储]
    D --> G[发送给XHS App]
    E --> G
    
    G --> H[用户授权]
    H --> I[返回authorization_code + state]
    
    I --> J[验证state参数]
    J --> K{state匹配?}
    
    K -->|是| L[使用code_verifier交换token]
    K -->|否| M[拒绝授权 - CSRF攻击]
    
    L --> N[服务器验证PKCE]
    N --> O[返回access_token]
    
    O --> P[AES加密存储token]
    
    style C fill:#ffcdd2
    style D fill:#ffcdd2  
    style E fill:#ffcdd2
    style J fill:#c8e6c9
    style N fill:#c8e6c9
    style P fill:#fff3e0
```

## 6. 错误处理流程

![错误处理流程](diagrams/06-error-handling.png)

```mermaid
graph TD
    A[SDK操作] --> B{检查配置}
    B -->|未配置| C[ERROR_NOT_CONFIGURED]
    B -->|已配置| D{检查XHS App}
    
    D -->|未安装| E[ERROR_APP_NOT_INSTALLED]
    D -->|不支持授权| F[ERROR_UNSUPPORTED]
    D -->|支持| G[启动授权流程]
    
    G --> H{授权结果}
    H -->|用户取消| I["onCancel()"]
    H -->|授权拒绝| J[ERROR_AUTH_DENIED]
    H -->|授权成功| K[交换token]
    
    K --> L{网络请求}
    L -->|失败| M[ERROR_NETWORK]
    L -->|成功| N{token有效}
    
    N -->|无效| O[ERROR_TOKEN_INVALID]
    N -->|有效| P[获取用户信息]
    
    P --> Q{用户信息}
    Q -->|失败| R[ERROR_USER_INFO_FAILED]
    Q -->|成功| S["onSuccess()"]
    
    style C fill:#ffcdd2
    style E fill:#ffcdd2
    style F fill:#ffcdd2
    style I fill:#fff3e0
    style J fill:#ffcdd2
    style M fill:#ffcdd2
    style O fill:#ffcdd2
    style R fill:#ffcdd2
    style S fill:#c8e6c9
```

## 7. 存储加密流程

![存储加密流程](diagrams/07-storage.png)

```mermaid
graph TD
    A[存储敏感数据] --> B["CryptoUtils.encrypt()"]
    B --> C[生成设备唯一密钥]
    C --> D[AES加密数据]
    D --> E[保存到SharedPreferences]
    
    F[读取敏感数据] --> G[从SharedPreferences读取]
    G --> H["CryptoUtils.decrypt()"]  
    H --> I[使用设备密钥解密]
    I --> J[返回明文数据]
    
    K[数据类型] --> L[access_token]
    K --> M[refresh_token] 
    K --> N[用户ID]
    K --> O[OpenID]
    
    style D fill:#fff3e0
    style I fill:#c8e6c9
    style L fill:#e3f2fd
    style M fill:#e3f2fd
    style N fill:#e3f2fd
    style O fill:#e3f2fd
```

## 8. SDK生命周期管理

![SDK生命周期管理](diagrams/08-lifecycle.png)

```mermaid
stateDiagram-v2
    [*] --> 未配置: 创建实例
    未配置 --> 已配置: "configure()"
    已配置 --> 登录中: "login()"
    登录中 --> 已登录: 授权成功
    登录中 --> 已配置: 授权失败/取消
    已登录 --> 已登录: "getUserInfo()"
    已登录 --> 已登录: "refreshToken() 成功"
    已登录 --> 已配置: "refreshToken() 失败"
    已登录 --> 已配置: "logout()"
    已配置 --> [*]: 应用销毁
```

## 关键设计说明

### PKCE安全增强
- 每次授权生成唯一的`code_verifier`和`code_challenge`
- 防止授权码拦截攻击，提升OAuth安全性

### App-to-App通信
- 使用ComponentName精确定位小红书OAuth Activity
- 通过Intent Extra传递结构化数据（Parcelable）
- XHSEntryActivity作为回调接收器处理授权结果

### 本地存储安全
- 使用AES加密存储所有敏感信息
- 设备相关密钥，提升数据安全性
- 支持清空所有用户数据（logout）

### 错误处理策略
- 分层错误处理：网络层、业务层、UI层
- 结构化错误码便于问题定位和用户体验优化
- 主线程回调确保UI操作安全性