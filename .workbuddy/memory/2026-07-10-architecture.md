# 媒体资讯平台架构概览

> 基于 codegraph 全量索引（168 files / 2713 nodes / 5165 edges）+ 源码逐文件阅读整理。
> 生成时间：2026-07-10

## 1. 一句话总结

**Spring Boot 3.5.3 + Java 21 多模块后端 + 两个独立 Vue 3 前端** 的媒体资讯系统，覆盖"管理员投稿/审核/发布"和"用户浏览/投稿/点赞/收藏"两套业务，配 Redis 缓存、JWT 双端鉴权、AOP 审计日志、Spring Task 定时统计、阿里云 OSS 文件上传、Spring WebSocket 实时审核通知。

## 2. 技术栈

### 后端（`com.connor.media-news`，Spring Boot 3.5.3 / Java 21 / `jakarta.*`）
| 维度 | 选型 |
|---|---|
| Web | spring-boot-starter-web（内嵌 Tomcat，端口 8080）|
| ORM | MyBatis 3.0.4 + MyBatis-Spring-Boot-Starter，PageHelper 2.1.1 |
| 持久化 | MySQL 8.x（驱动 `com.mysql.cj.jdbc.Driver`）|
| 缓存 | spring-data-redis（默认 `127.0.0.1:6379/db0`），GenericJackson2JsonRedisSerializer + default typing |
| 鉴权 | jjwt 0.12.6（HS256，单密钥 `media.jwt.secret-key`）|
| 密码 | spring-security-crypto 的 `BCryptPasswordEncoder`（仅用于密码哈希，无 Spring Security 全家桶）|
| 实时 | spring-boot-starter-websocket（`TextWebSocketHandler`）|
| 任务 | spring `@Scheduled`（`@EnableScheduling`）|
| AOP | spring-aop + aspectjweaver，两个自定义切面：`AutoFillAspect` / `LogRecordAspect` |
| 对象存储 | aliyun-sdk-oss 3.18.3 |
| JSON | jackson-databind + jsr310（全局 `JavaTimeModule`，日期不输出时间戳）|
| 工具 | Lombok（`@Data` 等注解处理器）|

### 前端
| 端 | 栈 | 端口 | 代理 |
|---|---|---|---|
| 管理端 `Frontend/admin` | Vue 3.4 + Vue-Router 4 + Pinia 2 + Element Plus 2.7 + ECharts 5 + wangEditor 5 + axios 1.7 | 5173 | `/admin` `/common` `/ws` → `http://localhost:8080` |
| 用户端 `Frontend/user` | Vue 3.4 + Vue-Router 4 + Pinia 2 + axios 1.6 + dayjs + qs | 5174 | `/user` `/ws` → `http://localhost:8080` |
| 构建 | Vite 5.x，`@` → `src` 别名，Sass（admin）|
| 鉴权约定 | 请求头 `token: {jwt}`（两端统一，与 `docs/api.md` 写的 `authentication` 不一致）|

## 3. 目录与模块

```
F:\JavaWebProjects\newsPlatform
├── pom.xml                           # 父 pom，统一管理版本与 5 个子模块
├── mvnw / mvnw.cmd                   # Maven Wrapper
├── README.md / 媒体资讯平台/ / docs/ # 项目文档、面试题、流程图、接口文档
├── sql/init.sql                      # 8 张表 + 种子数据（admin/zhangsan 同 BCrypt 哈希）
│
├── media-common   # 公共基础（无 Spring 启动）
│   ├── annotation/    # @AutoFill(INSERT/UPDATE), @LogRecord(value)
│   ├── aspect/        # AutoFillAspect, LogRecordAspect
│   ├── constant/      # ArticleStatus(0~5), CacheKeys, JwtClaimsConstant
│   ├── context/       # BaseContext (ThreadLocal<Long> id + String type)
│   ├── enumeration/   # OperationType (INSERT/UPDATE)
│   ├── exception/     # BusinessException
│   ├── properties/    # JwtProperties, OssProperties (@ConfigurationProperties)
│   ├── result/        # Result<T>(code/msg/data), PageResult<T>(total/records)
│   └── utils/         # JwtUtil, OssUtil, DateTimeUtil
│
├── media-domain   # PO/DTO/VO（无任何业务逻辑）
│   ├── po/   # NewsArticle, NewsCategory, SysUser, NewsUserInteraction,
│   │         # NewsReadRecord, NewsDailyStats, SysOperationLog
│   ├── req/  # ArticleDTO, AuditDTO, CategoryDTO, LoginDTO, UserProfileDTO, UserRegisterDTO
│   └── vo/   # ArticleVO, CategoryVO, UserVO, AdminUserVO, LoginVO, UploadVO,
│             # DashboardTodayVO, TrendVO, CategoryDistributionVO
│
├── media-dao      # MyBatis Mapper 接口（无实现类，依赖 mapper XML）
│   └── 7 个 Mapper 接口
│
├── media-service  # 业务层（接口 + 实现，事务、缓存、消息推送）
│   ├── impl/   # AuthServiceImpl, ArticleServiceImpl, CategoryServiceImpl,
│   │           # StatisticsServiceImpl, BeanCopy(包私有工具)
│   └── websocket/  # AuditWebSocketHandler
│
└── media-web      # 启动模块（Spring Boot 入口）
    ├── MediaNewsApplication.java  # @SpringBootApplication(scanBasePackages="com.connor.newsplatform")
    ├── config/  # WebMvcConfig, JwtTokenInterceptor, GlobalExceptionHandler,
    │            # RedisConfig, OssConfig, JacksonConfig, WebSocketConfig
    ├── controller/
    │   ├── admin/   # AdminAuth, AdminArticle, AdminCategory, AdminDashboard,
    │   │            # AdminStatistics, AdminLog
    │   ├── user/    # UserAuth, UserProfile, UserArticle, UserCategory
    │   └── common/  # UploadController
    ├── interceptor/ # JwtTokenInterceptor
    ├── task/        # NewsTask (@EnableScheduling)
    └── resources/
        ├── application.yml
        └── mapper/  # 4 个 MyBatis XML（NewsArticle/NewsCategory/SysUser/SysOperationLog）
```

**注意**：
- README 写 `media-server`、`com.connor.newsplatform.server.MediaNewsApplication`，但磁盘实际已重构为 `media-web` + `com.connor.newsplatform.web.MediaNewsApplication`（根 pom `<modules>` 也只列 5 个新名字）。**README 描述已过时**。
- `media-dao` 的 POM 引用了 `media-domain`，但启动类 `@MapperScan("com.connor.newsplatform.dao")` 在 `media-web` 中——这是常见做法，但要求 `media-web` 能看到 mapper 接口：依赖 `media-service` → `media-dao` 间接传递，OK。
- `MyBatis XML` 物理上放在 `media-web/src/main/resources/mapper/`，**不在 media-dao**。`application.yml` 的 `classpath:mapper/*.xml` 仍可命中（classpath 合并）。

## 4. 启动入口与配置

- **启动类**：`com.connor.newsplatform.web.MediaNewsApplication`（`media-web` 模块，`@SpringBootApplication` + `@MapperScan` + `@ConfigurationPropertiesScan` + `@EnableScheduling`）
- **服务端口**：`server.port=8080`
- **MySQL**：`jdbc:mysql://localhost:3306/news_db`，root / 000000，初始化脚本 `sql/init.sql` 会 drop & recreate
- **Redis**：`127.0.0.1:6379` db 0
- **JWT**：`media.jwt.secret-key=media-news-secret-key`（硬编码占位符），`ttl=86400000`（24h，与代码里 7200000L 默认值不一致，按 yml 走）
- **OSS**：`media.oss.*` 占位符值（`your-endpoint` 等），`OssUtil.validateProperties()` 会拒绝以 `your-` 开头的值，**不配真凭证无法上传**
- **JVM**：Java 21，`<release>21</release>`

## 5. 核心组件

### 5.1 横切组件（media-common）

| 组件 | 职责 | 关键细节 |
|---|---|---|
| `Result<T>` | 统一响应：`{code:1, msg:"success", data}`；错误用 `code:0` | 业务异常返回 `code:0`，鉴权失败返回 `code:401` |
| `PageResult<T>` | 分页：`{total, records}` | PageHelper 输出的 `Page<>.getTotal()/getResult()` 转换 |
| `BaseContext` | `ThreadLocal<Long> id` + `ThreadLocal<String> type` | 由 `JwtTokenInterceptor` 写入；`afterCompletion` 清理 |
| `@AutoFill` + `AutoFillAspect` | 在 Service 第一个 Entity 参数上反射注入 `create/update*` 字段 | 反射调用，**当前 Service 实现大多没用此注解**，而是直接 `setCreateTime/setCreateUser`（注解被遗留） |
| `@LogRecord` + `LogRecordAspect` | `@AfterReturning` 写 `sys_operation_log` | 用 `ObjectProvider<JdbcTemplate>` 拿 JdbcTemplate 写日志，不走 MyBatis |
| `JwtUtil` | jjwt 0.12 API：builder + parser；密钥 < 32 byte 时右侧补 0 | 单密钥、无 jti、无 refresh token |
| `OssUtil` | 阿里云 OSS 客户端；`yyyy/MM/{uuid}.{ext}` 路径；占位符校验 | `new OSSClientBuilder().build(...)`，**未做连接池/复用** |
| `BusinessException` | 唯一业务异常类型 | 全局 `GlobalExceptionHandler.business` 返回 `code:0` |
| `ArticleStatus` | 稿件状态枚举值 `0~5` | DRAFT(0) / PENDING(1) / APPROVED(2) / REJECTED(3) / PUBLISHED(4) / OFFLINE(5) |
| `CacheKeys` | Redis key 常量 | `media:category:list` / `media:article:detail:{id}` / `media:dashboard:today` |

### 5.2 鉴权链（`JwtTokenInterceptor` + `WebMvcConfig`）

```
HTTP 请求
  ↓
CORS（WebMvcConfig：放行所有 Origin、OPTIONS 直接过）
  ↓
匹配路径前缀
  ├── /admin/**         排除 /admin/auth/login
  └── /user/**          排除 /user/auth/{register,login}, /user/category/list, /user/article/page
      ↓
读取请求头 token
  ├── 缺失：
  │   ├── /user/article/** 放行（可匿名浏览）
  │   └── 其他 → 抛 IllegalArgumentException → 全局处理 → code:401
  └── 存在：
      ├── JwtUtil.parseJwt 验签 + 过期
      ├── claims.userType 与 路径前缀不匹配 → 401（防止 user token 调 admin 接口）
      ├── BaseContext.setCurrentId(uid) / setCurrentType(userType)
      └── 控制器处理
afterCompletion → BaseContext.removeCurrent()（防止 ThreadLocal 泄漏）
```

**JWT claims**：`userId`(Long) / `userType`(admin/user) / `username`(String)

### 5.3 Service 层（4 个核心服务）

#### AuthServiceImpl
- `adminLogin / userLogin`：BCrypt 校验 → 状态 0 = 禁用 → 签发 JWT
- `register`：BCrypt 加密 → `user_type='user'` → 写 sys_user
- `currentAdmin/currentUser`：从 BaseContext 读 uid → sys_user
- `updateUser/updateAdminStatus`：动态 SQL 仅更新非空字段
- **未实现**：refresh token、找回密码、注销 token

#### ArticleServiceImpl（最复杂，13+ Mapper 方法）
**管理端流程**：
- `adminPage`：PageHelper + 7 维筛选（title/categoryId/status/时间范围）
- `createDraft`：拷贝 DTO → `status=DRAFT` → 计数字段默认 0 → 入库
- `updateArticle`：分两步（先 update 元数据，再 update content）—— 注释已说明"正文已并入 news_article"
- `deleteArticle`：先删 `news_user_interaction` 再删 `news_article`
- `submit`：status → PENDING → WebSocket 广播管理员
- `approve / reject`：更新 status → `pushAuditMessage` 同时推 admin + 作者
- `publish`：仅 APPROVED/OFFLINE 可发布 → 写 `publish_time` → 通知作者
- `offline`：→ OFFLINE → 通知作者

**用户端流程**：
- `userPage`：PageHelper + 关键词模糊
- `userDetail`：status 必须是 PUBLISHED
- `vote(id, ±1)`：互斥逻辑——同点取消、反向切换——事务包 4 个 Mapper 写
- `toggleFavorite`：幂等切换
- `recordRead`：用 `news_read_record.read_date = DATE(create_time) STORED` + UNIQUE(user,article,date) 实现"同用户同日同篇 1 次"

**WebSocket 消息构造**：`buildMessage` 手拼 JSON 字符串（**未做 JSON 转义**，含标题/内容有 `"` 风险），消息形如：
```json
{"type":"USER_ARTICLE_SUBMIT","title":"...","content":"...","time":"yyyy-MM-dd HH:mm:ss"}
```

**`toVO` 填充**（每次 `userPage/adminPage/userDetail` 都会走）：
- 查 category（`categoryMapper.getById` 单次）
- 查作者（`sysUserMapper.getById` 单次，**`resolveAuthorName` 还会再查一次**）—— N+1
- 查当前用户 like/dislike/favorite（登录态下 3 次交互查询）—— 又一组 N+1

#### CategoryServiceImpl
- 启用分类列表走 Redis 缓存（`media:category:list`），TTL 24h，增删改清缓存
- 名称唯一性由 SQL UNIQUE KEY 兜底（Mapper 显式 `countByName` 预检）

#### StatisticsServiceImpl
- 今日看板走 Redis（TTL 5min），`Key=media:dashboard:today`
- 趋势 = `news_daily_stats` 范围查询 + 缺失日补 0
- 分类分布 = 查所有已发布 → 分组计数 → JOIN 分类名（**全量加载到内存**，无分页）
- `generateDailyStats` 由 `NewsTask` 每天 00:10 触发，覆盖写入昨日统计

### 5.4 WebSocket 推送

```
后端 AuditWebSocketHandler
  ├── Map<Long, WebSocketSession> adminSessions  // /ws/audit/{adminId}
  ├── Map<Long, WebSocketSession> userSessions   // /ws/notify/{type}/{id}
  ├── sendToAdmin / sendToUser / broadcastAdmins
  └── 触发源：ArticleServiceImpl、NewsTask
前端 utils/websocket.js (admin)
  ├── 自动重连（最多 5 次，3s 间隔）
  ├── ElNotification 弹窗
  └── 路由 /ws/audit/{adminId} （注意：用户端连的是 /ws/notify/user/{id}，但 AdminArticleController 内部通知用户走 sendToUser(id) 走 /ws/notify 端点）
```

**注意**：当前 `ArticleServiceImpl.notifyUser` 调的是 `webSocketHandler.sendToUser(userId, message)`，但**前端 `websocket.js` 只连了 admin 端点**，用户端没有 WebSocket 连接实现——**用户的实时通知目前收不到**。

### 5.5 控制器清单（22 个端点）

#### 管理端（`/admin/...`，全部走 JwtTokenInterceptor）
| 模块 | 端点 | 方法 | 备注 |
|---|---|---|---|
| auth | `/auth/login` | POST | 排除拦截 |
| auth | `/auth/logout` | POST | `@LogRecord` |
| auth | `/auth/info` | GET | |
| auth | `/auth/status/{status}?id=` | POST | `@LogRecord` |
| article | `/article/page` | GET | |
| article | `/article/{id}` | GET | |
| article | `/article` | POST/PUT | `@LogRecord` |
| article | `/article/{id}` | DELETE | `@LogRecord` |
| article | `/article/{id}/{submit,approve,reject,publish,offline}` | POST | 全部 `@LogRecord` |
| category | `/category/page` | GET | |
| category | `/category` `/category/{id}` `/category/status/{status}?id=` | CRUD | 全部 `@LogRecord` |
| dashboard | `/dashboard/today` | GET | Redis 缓存 |
| statistics | `/statistics/{read,publish}?begin=&end=` | GET | |
| statistics | `/statistics/category-distribution` | GET | |
| log | `/log/page` | GET | |

#### 用户端（`/user/...`）
| 模块 | 端点 | 方法 | 备注 |
|---|---|---|---|
| auth | `/auth/register` `/auth/login` | POST | 排除拦截 |
| user | `/user/info` | GET/PUT | |
| category | `/category/list` | GET | 排除拦截，Redis 缓存 |
| article | `/article/page` | GET | 排除拦截 |
| article | `/article/{id}` | GET | |
| article | `/article/mine/page` | GET | |
| article | `/article/favorite/page` | GET | |
| article | `/article` | POST | `@LogRecord`（用户投稿） |
| article | `/article/{id}/{like,dislike,favorite,read}` | POST | 全部 `@LogRecord` |

#### 通用
| 模块 | 端点 | 方法 | 备注 |
|---|---|---|---|
| common | `/common/upload` | POST | 走 `OssUtil`，**未挂拦截器**——任何人都能上传，需要鉴权时建议挂上 |

### 5.6 启动流程
1. Spring Boot 启动 → `@MapperScan` 扫 `com.connor.newsplatform.dao` → MyBatis 加载 `classpath:mapper/*.xml`
2. `@ConfigurationPropertiesScan` 加载 JwtProperties/OssProperties
3. Web 容器启动 → 注册 `JwtTokenInterceptor`（`/admin/**` + `/user/**`）
4. WebSocket 注册到 `/ws/audit/{adminId}` 和 `/ws/notify/{type}/{id}`
5. `@EnableScheduling` 启动 `NewsTask` 调度

## 6. 核心业务流

### 6.1 管理员稿件生命周期

```
[管理端] createDraft  (status=0 DRAFT)
        └─@LogRecord "新增稿件草稿"
        ↓ 可选
[管理端] updateArticle
        ↓
[管理端] submit  (status=1 PENDING)
        └─WebSocket 广播管理员 "ARTICLE_PENDING"
        ↓
[管理端] approve (status=2 APPROVED)  ←─  reject (status=3 REJECTED, 写 reject_reason)
        └─@LogRecord + WebSocket 通知 admin + 作者
        ↓
[管理端] publish (status=4 PUBLISHED, 写 publish_time)
        └─WebSocket 通知作者 "ARTICLE_PUBLISHED"
        ↓
[用户端] 看到文章 → userDetail / recordRead
        ↓ 可下架
[管理端] offline (status=5 OFFLINE)
        └─WebSocket 通知作者
```

### 6.2 用户投稿流
```
[用户端] POST /user/article
       → ArticleServiceImpl.submitUserArticle
         ├─ status=1 PENDING
         ├─ INSERT news_article
         └─ WebSocket 广播管理员 "USER_ARTICLE_SUBMIT"
                  ↓
       [管理端] 收到通知 → ArticleAudit.vue 审核 → approve / reject
```

### 6.3 点赞互斥逻辑
```
点 like:
  existing=like?  → 取消 like，count-1
  existing=dislike? → 取消 dislike(count-1) + 新增 like(count+1)
  都没有          → 新增 like(count+1)
点 dislike 同理对称
```
**全部在一个 `@Transactional` 内**（`ArticleServiceImpl.vote`）。

### 6.4 登录流
```
[前端] POST /user/auth/login 或 /admin/auth/login
[后端] AuthServiceImpl.userLogin/adminLogin
       ├─ SysUserMapper.getByUsernameAndType(BCrypt check)
       ├─ status=1 ?
       └─ JwtUtil.createJwt(secret, ttl, {userId, userType, username})
[前端] 存 token → 后续所有请求 headers['token'] = token
[后端] JwtTokenInterceptor 验签 → BaseContext 注入
```

## 7. 数据模型

8 张核心表（`sql/init.sql`）：

```
sys_user            # 合并原 admin_user/app_user，user_type 区分
news_category       # 分类，含 status、sort
news_article        # 文章+正文(content LONGTEXT)；status 0~5
news_user_interaction  # 合并原 vote/favorite，action_type 区分 like/dislike/favorite
                       # UNIQUE(user, article, action)
news_read_record    # 阅读记录；read_date 由 DATE(create_time) STORED 生成
                    # UNIQUE(user, article, read_date)  实现"同用户同日同篇1次"
news_article_comment  # ⚠️ 历史遗留表，代码已不引用（前端没评论 API）
news_daily_stats    # 每日统计（定时任务生成）
sys_operation_log   # 操作审计日志（@LogRecord 触发）
```

**外键**全部省略，靠应用层保证一致性。

## 8. 文档与代码的不一致（潜在 bug）

1. **README.md 写 `media-server` / `com.connor.newsplatform.server.MediaNewsApplication`**，但磁盘上是 `media-web` / `com.connor.newsplatform.web.MediaNewsApplication`。启动命令 `mvnw -pl media-server -am` 不可用。
2. **`docs/api.md` 说请求头是 `authentication`**，但后端 `JwtTokenInterceptor` 读的是 `token`（前端两个 `utils/request.js` 也都用 `token`）——文档与实现全部以实现为准。
3. **`docs/api.md` 列出 `/admin/statistics/hot`、`/user/article/hot`**，但代码里没有这两个端点，没有 `hot` 排序算法实现（`媒体资讯平台/推荐排序与热度算法说明.md` 有文档，但代码不存）。
4. **Dashboard.vue 期望 `getReadStatistics` 返回 `{dates, values}`**，但后端返回的是 `List<TrendVO({date, value})>` —— 图表会取不到数据（前端有 `||` 兜底，会显示空数组）。
5. **`@AutoFill` 注解在 Service 实现里几乎没人用**——`setCreateMeta()` 手工赋值代替。这是死代码，删掉 AOP 也行。
6. **`com.connor.newsplatform.web.task.NewsTask` 依赖了 `AuditWebSocketHandler`**，但用 `broadcast` 发给所有管理员，cron `0 10 0 * * ?` 凌晨 0:10 跑，但 broadcast 实现是 `adminSessions.forEach(sendToAdmin)` —— 没人订阅时静默丢弃，**凌晨管理员不会收到任何通知**（因为没人连 WS）。
7. **用户端 WS 客户端实际是有的**（`Frontend/user/src/utils/websocket.js` + `user store` 在 `login/fetchUserInfo/logout` 里调 connect/disconnect），缺的是 **App.vue 启动时如果有 token 自动重连**（刷新页面后 WS 会断），已经补上。
8. **MyBatis XML 物理上在 `media-web/src/main/resources/mapper/`**，不在 media-dao（从分层角度应挪到 media-dao）。

## 9. 技术债务 / 改进建议

### 高优先级
- **同步文档**：README 改成 `media-web` + `com.connor.newsplatform.web.MediaNewsApplication`；`docs/api.md` 改成 `token` 请求头。
- **Dashboard 数据契约**：要么后端返回 `{dates, values}`，要么前端解析 `List<TrendVO>`。否则趋势图永远空。
- **`/common/upload` 加鉴权**：目前未挂拦截器，任何人都能上传消耗 OSS 流量。建议加 `@LogRecord` + JwtTokenInterceptor include `/common/**`，按 userType 区分。
- **统一 N+1 查询**：`ArticleServiceImpl.toVO` 在列表场景下 N 次 `categoryMapper.getById` + N 次 `sysUserMapper.getById` + 3N 次交互查询，应该改用 IN 查询一次拉。
- **删除 `news_article_comment` 表**：无引用，是死数据。或者把评论功能补回来。
- **WebSocket 用户端**：补 `Frontend/user/src/utils/websocket.js` 连 `/ws/notify/user/{userId}`，否则 `notifyUser` 推送形同虚设。
- **死代码清理**：`@AutoFill` + `AutoFillAspect`、`StatisticsServiceImpl` 里的 `articleService` 依赖注入但未使用。

### 中优先级
- **OSS 连接池**：每次上传 `new OSSClientBuilder().build`，调用完 `shutdown`。建议改成 `OSS` 单例 Bean。
- **`OssUtil.validateProperties` 硬编码 "your-" 前缀判断**，与配置中心的占位符耦合。可以加一个 `@ConditionalOnProperty(prefix="media.oss", matchIfMissing=false)` 直接在缺配时禁用 Bean。
- **JWT 单密钥 + 无 jti**：补 refresh token、token 撤销（黑名单走 Redis）。
- **`GlobalExceptionHandler` 中 `IllegalArgumentException` 被吞成 401**，但 `BusinessException` 已被单独处理，结构 OK；但 `MethodArgumentNotValidException` 用 `findFirst()` 只返第一条校验错，不友好。
- **`StatisticsServiceImpl.today()` 5min 缓存**，但管理员操作后实时性差（投稿、审核后不会立即反映）。建议加 `@CacheEvict` 或者缓存预热。
- **手动拼 JSON 字符串**（`buildMessage`）：用 `ObjectMapper` 代替，避免引号转义 bug。
- **`@LogRecord` 用 JdbcTemplate 写日志**，但 Service 内已经用 MyBatis。统一走 MyBatis 性能更好。
- **`@LogRecord` 没有记失败日志**，只有 `@AfterReturning`（成功才记），建议加 `@AfterThrowing` 记失败。
- **密码字段返回 OK**：`UserVO` / `AdminUserVO` 是显式列字段（不是 `BeanUtils.copyProperties` 整对象），**不会把 `sys_user.password` 返回给前端**。
- **CORS `allowedOriginPatterns("*") + allowCredentials(true)`**：在生产里不推荐（*+credentials 是浏览器规范违规，会被拒绝），需要收紧。

### 低优先级
- **MyBatis 日志用 `StdOutImpl`**，生产应换成 `SLF4JImpl`。
- **application.yml 的 `media.jwt.secret-key` 是占位符**且有默认值（`JwtProperties.java` 里 `secretKey = "media-news-secret-key"`），启动不会报错但不安全。应改为 `media.jwt.secret-key:` 无默认，启动时强制提供。
- **无限滚动基于 `IntersectionObserver` + `200px rootMargin`**（Home.vue），与 PageHelper 分页配合，但每次 loadMore 都重新拿当前页 `articleList.length` 算 `hasMore`，能跑但不是最简。
- **sql 脚本里 admin / zhangsan 用了同一个 BCrypt 哈希**（`$2a$10$2QIWdtDgCc4eNDh62tdpi.Lj.04BibsiW15ltXxk7v2UZZnINZfge`），可以反查原文，但应该明文给个提示或换成不同密码。
- **缺单元测试**：只有 `media-web/src/test/java/...` 空目录，0 测试用例。codegraph 提示 `no covering tests found` 在每个 Service/Mapper 上都成立。
- **缺 `media-dao` 的 mapper XML**（XML 实际在 `media-web`），从分层角度 XML 应该和 Mapper 接口同模块（`media-dao`）。建议把 XML 移到 `media-dao/src/main/resources/mapper/`，跟 Mapper 一起打包。

## 10. 关键入口与文件清单（速查）

| 角色 | 文件 |
|---|---|
| 启动类 | `media-web/src/main/java/com/connor/newsplatform/web/MediaNewsApplication.java` |
| 应用配置 | `media-web/src/main/resources/application.yml` |
| MyBatis XML | `media-web/src/main/resources/mapper/{NewsArticle,NewsCategory,SysUser,SysOperationLog}Mapper.xml` |
| 鉴权拦截器 | `media-web/.../interceptor/JwtTokenInterceptor.java` |
| 全局异常 | `media-web/.../config/GlobalExceptionHandler.java` |
| WebSocket 注册 | `media-web/.../config/WebSocketConfig.java` |
| WebSocket Handler | `media-service/.../websocket/AuditWebSocketHandler.java` |
| 定时任务 | `media-web/.../task/NewsTask.java` |
| AOP 自动填充 | `media-common/.../aspect/AutoFillAspect.java` |
| AOP 审计日志 | `media-common/.../aspect/LogRecordAspect.java` |
| JWT 工具 | `media-common/.../utils/JwtUtil.java` |
| OSS 工具 | `media-common/.../utils/OssUtil.java` |
| 业务核心 | `media-service/.../impl/{Auth,Article,Category,Statistics}ServiceImpl.java` |
| SQL | `sql/init.sql` |
| 前端 admin 路由 | `Frontend/admin/src/router/index.js` |
| 前端 user 路由 | `Frontend/user/src/router/index.js` |
| 前端 admin WS 客户端 | `Frontend/admin/src/utils/websocket.js` |
| 前端 user 鉴权拦截 | `Frontend/user/src/router/index.js` (`requiresAuth`) |
| 前端 admin 鉴权拦截 | `Frontend/admin/src/router/index.js` (`isLogin`) + 后端 `JwtTokenInterceptor` |
| 文档 | `媒体资讯平台/` 下 13 份 .md/.mermaid（流程图、面试题、提示词、接口表、热度算法） |
