## AOP 切面工作流程

本项目使用 Spring AOP 实现了两个通用切面：**公共字段自动填充**（@AutoFill）和**操作日志自动记录**（@LogRecord）。两个切面都定义在 `media-common` 模块中，所有业务模块共享使用。

---

### 一、@AutoFill —— 公共字段自动填充

#### 解决的问题

`news_article`、`news_category`、`admin_user` 等业务表都有 `create_time`、`update_time`、`create_user`、`update_user` 四个公共字段。如果在每个 Service 方法中手动赋值，代码冗余且容易遗漏。`@AutoFill` 通过 AOP 在方法执行前自动填充这些字段。

#### 涉及的文件

```
media-common/
  annotation/AutoFill.java      ← 自定义注解
  enumeration/OperationType.java ← 操作类型枚举（INSERT / UPDATE）
  aspect/AutoFillAspect.java    ← 切面实现
  context/BaseContext.java      ← ThreadLocal，提供当前用户ID
```

#### 注解定义

```java
@Target(ElementType.METHOD)       // 只能加在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
public @interface AutoFill {
    OperationType value();          // INSERT 或 UPDATE
}
```

#### 切面逻辑（AutoFillAspect）

```java
@Aspect
@Component
public class AutoFillAspect {

    @Pointcut("@annotation(com.connor.newsplatform.common.annotation.AutoFill)")
    public void autoFillPointcut() {}

    @Before("autoFillPointcut() && @annotation(autoFill)")
    public void fill(JoinPoint joinPoint, AutoFill autoFill) {
        // 1. 获取方法的第一个参数（约定为实体对象）
        Object entity = joinPoint.getArgs()[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 2. 根据操作类型填充不同字段
        if (autoFill.value() == OperationType.INSERT) {
            // INSERT：填充全部四个字段
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setCreateUser(currentId);
            entity.setUpdateUser(currentId);
        } else {
            // UPDATE：只填充更新相关字段
            entity.setUpdateTime(now);
            entity.setUpdateUser(currentId);
        }
    }
}
```

注意：实际代码中通过**反射**调用 setter 方法（`invoke`），因为切面不知道实体的具体类型。如果实体没有某个 setter（比如 `app_user` 表没有 `create_user` 字段），反射调用会静默跳过，不会报错。

#### 使用方式

在 Mapper 接口的方法上标注注解：

```java
public interface ArticleMapper extends BaseMapper<NewsArticle> {

    @AutoFill(OperationType.INSERT)
    int insert(NewsArticle article);

    @AutoFill(OperationType.UPDATE)
    int update(NewsArticle article);
}

public interface CategoryMapper extends BaseMapper<NewsCategory> {

    @AutoFill(OperationType.INSERT)
    int insert(NewsCategory category);

    @AutoFill(OperationType.UPDATE)
    int update(NewsCategory category);
}
```

#### 执行流程

```
Service 调用 mapper.insert(article)
  │
  AOP 拦截：@Before 触发 AutoFillAspect.fill()
  │
  ├── 读取操作类型：INSERT
  ├── 获取当前时间：LocalDateTime.now()
  ├── 获取当前用户：BaseContext.getCurrentId()
  ├── 反射填充：
  │     ├── article.setCreateTime(now)
  │     ├── article.setUpdateTime(now)
  │     ├── article.setCreateUser(currentId)
  │     └── article.setUpdateUser(currentId)
  │
  MyBatis Plus 执行 INSERT SQL（此时 entity 已包含公共字段值）
  │
  数据写入数据库 ✓
```

---

### 二、@LogRecord —— 操作日志自动记录

#### 解决的问题

管理端的每个写操作（新增文章、审核通过、删除分类等）都需要记录操作日志到 `sys_operation_log` 表。如果在每个 Controller 方法中手动写日志代码，侵入性强且容易遗漏。`@LogRecord` 通过 AOP 在方法执行成功后自动记录日志。

#### 涉及的文件

```
media-common/
  annotation/LogRecord.java     ← 自定义注解
  aspect/LogRecordAspect.java   ← 切面实现
  context/BaseContext.java      ← ThreadLocal，提供当前用户ID和类型
media-pojo/
  entity/SysOperationLog.java   ← 日志实体类
数据库：
  sys_operation_log 表           ← 日志存储
```

#### 注解定义

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogRecord {
    String value();  // 操作名称，如 "新增文章"、"审核通过"
}
```

#### 切面逻辑（LogRecordAspect）

```java
@Aspect
@Component
public class LogRecordAspect {

    @Pointcut("@annotation(com.connor.newsplatform.common.annotation.LogRecord)")
    public void logPointcut() {}

    @AfterReturning("logPointcut() && @annotation(logRecord)")
    public void record(JoinPoint joinPoint, LogRecord logRecord) {
        // 1. 获取当前 HTTP 请求
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();

        // 2. 序列化方法参数（过滤掉 MultipartFile，避免大文件序列化）
        String params = objectMapper.writeValueAsString(
                Arrays.stream(joinPoint.getArgs())
                      .filter(arg -> !(arg instanceof MultipartFile))
                      .toList()
        );

        // 3. 通过 JdbcTemplate 直接插入日志（不走 MyBatis Plus，避免循环依赖）
        jdbcTemplate.update(
            "INSERT INTO sys_operation_log " +
            "(operator_id, operator_type, operation, request_uri, " +
            " request_method, request_params, ip, create_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            BaseContext.getCurrentId(),      // 操作人ID
            BaseContext.getCurrentType(),    // 操作人类型
            logRecord.value(),              // 操作名称（注解的 value）
            request.getRequestURI(),        // 请求路径
            request.getMethod(),            // 请求方法（GET/POST/PUT/DELETE）
            params,                         // 请求参数（JSON）
            request.getRemoteAddr(),        // 操作者IP
            LocalDateTime.now()             // 操作时间
        );
    }
}
```

关键设计选择：
- 使用 `@AfterReturning` 而非 `@After`：只有方法**成功执行**后才记录日志，异常不记录
- 使用 `JdbcTemplate` 直接写 SQL：避免依赖 Mapper 层，减少耦合
- 过滤 `MultipartFile`：文件上传参数序列化会导致内存溢出

#### 使用方式

在 Controller 方法上标注注解：

```java
@RestController
@RequestMapping("/admin/article")
public class ArticleController {

    @LogRecord("新增文章")
    @PostMapping
    public Result<Long> addArticle(@RequestBody ArticleDTO dto) { ... }

    @LogRecord("提交审核")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) { ... }

    @LogRecord("审核通过")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody AuditDTO dto) { ... }

    @LogRecord("审核驳回")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody AuditDTO dto) { ... }

    @LogRecord("发布文章")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) { ... }

    @LogRecord("下架文章")
    @PostMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) { ... }
}
```

#### 执行流程

```
管理端发起 POST /admin/article（新增文章）
  │
  JwtTokenInterceptor → BaseContext 写入当前管理员信息
  │
  Controller.addArticle() 执行
  │  ├── 校验参数
  │  ├── 调用 Service 写入数据库
  │  └── 返回 Result.success(articleId)
  │
  AOP 拦截：@AfterReturning 触发 LogRecordAspect.record()
  │
  ├── 获取 HttpServletRequest
  │     ├── URI: /admin/article
  │     ├── Method: POST
  │     └── IP: 192.168.1.100
  │
  ├── 获取 BaseContext 信息
  │     ├── operatorId: 1
  │     └── operatorType: "admin"
  │
  ├── 获取注解 value: "新增文章"
  │
  ├── 序列化方法参数 → JSON 字符串
  │
  └── JdbcTemplate 执行 INSERT
        → sys_operation_log 表新增一条记录
```

---

### 三、两个切面的协作关系

```
请求进入
  │
  JwtTokenInterceptor.preHandle()
  │  └── BaseContext.setCurrentId(userId)
  │  └── BaseContext.setCurrentType("admin")
  │
  Controller 方法执行
  │  │
  │  ├── @AutoFill 触发（@Before）
  │  │     └── 填充 entity 的 createTime/updateTime/createUser/updateUser
  │  │
  │  ├── Service 调用 Mapper 执行 SQL
  │  │
  │  └── @LogRecord 触发（@AfterReturning）
  │        └── 从 BaseContext 读取 userId + userType
  │        └── INSERT INTO sys_operation_log
  │
  JwtTokenInterceptor.afterCompletion()
  │  └── BaseContext.removeCurrent()  ← 清理 ThreadLocal
```

`BaseContext` 是两个切面的桥梁：拦截器写入用户信息 → `@AutoFill` 读取 userId 填充公共字段 → `@LogRecord` 读取 userId + userType 记录操作日志 → 拦截器最终清理 ThreadLocal。

---

### 四、日志查询

管理端审计日志页面通过以下接口查询：

```
GET /admin/log/page
参数: page, pageSize, operation（操作名称关键词）, beginTime, endTime

返回: PageResult<SysOperationLog>
字段: operatorId, operatorType, operation, requestUri, requestMethod, requestParams, ip, createTime
```

前端 `OperationLog.vue` 页面展示操作人、操作类型、请求路径、请求方法、IP 和操作时间，支持按操作名称和时间范围筛选。
