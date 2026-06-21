## JWT 多端鉴权工作流程

本项目采用 JWT（JSON Web Token）实现管理端和用户端的双端鉴权。两端使用不同的请求头、不同的密钥、不同的用户类型标识，通过同一个拦截器统一处理。

---

### 整体架构

```
前端请求
  │
  ├── 管理端: Header "token: {jwt}"    ──→ /admin/**  ──→ JwtTokenInterceptor ──→ BaseContext ──→ Controller
  │
  └── 用户端: Header "authentication: {jwt}" ──→ /user/**   ──→ JwtTokenInterceptor ──→ BaseContext ──→ Controller
```

---

### 第一步：登录时生成 Token

登录接口（`/admin/auth/login` 或 `/user/auth/login`）不经过拦截器，由 `AuthServiceImpl` 直接处理。

**管理端登录流程：**

1. 接收 `LoginDTO`（username + password）
2. 通过 MyBatis Plus 查询 `admin_user` 表
3. 用 BCrypt 校验密码
4. 校验账号状态（status=1 才允许登录）
5. 调用 `JwtUtil.createJwt()` 生成 Token
6. 返回 `LoginVO`（包含 id、username、name、role、token）

**用户端登录流程：**

1. 接收 `LoginDTO`（username + password）
2. 通过 MyBatis Plus 查询 `app_user` 表
3. 密码校验支持 BCrypt 加密密码和明文密码两种格式（兼容旧数据）
4. 校验账号状态
5. 调用 `JwtUtil.createJwt()` 生成 Token
6. 返回 `LoginVO`（包含 id、username、nickname、avatar、token）

**Token 生成核心代码（JwtUtil.createJwt）：**

```java
// Payload 包含三个声明
claims: {
    "userId": 1,          // 用户ID
    "userType": "admin",  // 或 "user"
    "username": "admin"   // 用户名
}

// 签名配置
secretKey: "media-admin-secret"  // 管理端密钥
           "media-user-secret"   // 用户端密钥
ttl: 7200000ms（2小时）
```

管理端和用户端使用**不同的密钥**签名，所以管理端的 token 无法用于用户端接口，反之亦然。

---

### 第二步：请求拦截与鉴权

`WebMvcConfig` 注册了两个拦截器规则：

**管理端拦截规则：**
```
拦截路径: /admin/**
排除路径: /admin/auth/login（登录接口不需要鉴权）
```

**用户端拦截规则：**
```
拦截路径: /user/**
排除路径:
  - /user/auth/register（注册）
  - /user/auth/login（登录）
  - /user/category/list（分类列表，允许未登录访问）
  - /user/article/page（文章列表，允许未登录访问）
  - /user/article/hot（热点排行，允许未登录访问）
```

**拦截器处理流程（JwtTokenInterceptor.preHandle）：**

```
请求进入
  │
  ├── OPTIONS 预检请求？ ──→ 直接放行
  │
  ├── 判断请求路径
  │     ├── /admin/** → 从 Header "token" 取值
  │     └── /user/**  → 从 Header "authentication" 取值
  │
  ├── Token 为空？
  │     ├── 是 → 判断是否为可选鉴权路径（/user/article/**）
  │     │         ├── 是 → 放行（未登录也能看文章详情）
  │     │         └── 否 → 抛出 "未登录或登录已过期"
  │     └── 否 → 继续解析
  │
  ├── 选择密钥
  │     ├── admin → adminSecretKey
  │     └── user  → userSecretKey
  │
  ├── 解析 JWT（JwtUtil.parseJwt）
  │     ├── 验签失败 → 抛出异常
  │     └── 过期 → 抛出异常
  │
  ├── 校验 userType
  │     ├── 路径是 /admin/** 但 token 里 userType ≠ "admin" → 拒绝
  │     └── 路径是 /user/** 但 token 里 userType ≠ "user" → 拒绝
  │
  └── 写入 ThreadLocal
        ├── BaseContext.setCurrentId(userId)
        └── BaseContext.setCurrentType("admin" 或 "user")
        └── 放行请求 → Controller
```

---

### 第三步：Controller/Service 获取当前用户

请求通过拦截器后，`BaseContext`（ThreadLocal）中已存储了当前用户信息：

```java
// 在任何 Service 中获取当前用户
Long currentUserId = BaseContext.getCurrentId();      // 用户ID
String userType = BaseContext.getCurrentType();        // "admin" 或 "user"

// 典型用法：查询当前管理员信息
AdminUser user = adminUserMapper.selectById(BaseContext.getCurrentId());

// 典型用法：文章关联当前作者
article.setAuthorId(BaseContext.getCurrentId());
article.setAuthorType(BaseContext.getCurrentType());
```

---

### 第四步：请求结束后清理 ThreadLocal

```java
// JwtTokenInterceptor.afterCompletion
@Override
public void afterCompletion(...) {
    BaseContext.removeCurrent();  // 清除 ThreadLocal，防止内存泄漏
}
```

这一步非常关键。如果不清理，在 Tomcat 线程池复用时，下一个请求可能读到上一个请求的用户信息，导致越权操作。

---

### BaseContext 源码

```java
public final class BaseContext {
    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TYPE = new ThreadLocal<>();

    public static void setCurrentId(Long id) { CURRENT_ID.set(id); }
    public static Long getCurrentId() { return CURRENT_ID.get(); }
    public static void setCurrentType(String type) { CURRENT_TYPE.set(type); }
    public static String getCurrentType() { return CURRENT_TYPE.get(); }
    public static void removeCurrent() {
        CURRENT_ID.remove();
        CURRENT_TYPE.remove();
    }
}
```

---

### 完整请求生命周期

```
1. 前端发起请求（携带 token/authentication 请求头）
       │
2. JwtTokenInterceptor.preHandle()
   ├── 解析请求头获取 token
   ├── 根据路径选择密钥
   ├── JwtUtil.parseJwt() 验签 + 解析 payload
   ├── 校验 userType 是否匹配
   ├── BaseContext.setCurrentId() + setCurrentType()
   └── 放行
       │
3. Controller 处理业务
   ├── 通过 BaseContext.getCurrentId() 获取当前用户
   ├── 执行数据库操作
   └── 返回 Result<T>
       │
4. JwtTokenInterceptor.afterCompletion()
   └── BaseContext.removeCurrent() 清理 ThreadLocal
```

---

### 安全设计要点

1. **双密钥隔离**：管理端和用户端使用不同的 `secretKey`，token 不可跨端使用
2. **userType 校验**：即使伪造了有效签名，`userType` 不匹配也会被拒绝
3. **ThreadLocal 清理**：`afterCompletion` 中必然清理，避免线程复用导致的越权
4. **可选鉴权**：文章详情等接口允许未登录访问，但登录用户会获得更完整的体验（如阅读记录）
5. **密码加密**：新注册用户使用 BCrypt 加密，同时兼容明文密码的旧数据
