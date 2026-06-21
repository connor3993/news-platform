# 媒体资讯平台后端

Spring Boot 4.x + Java 21 多模块后端，包含管理端接口、用户端接口、JWT 多端鉴权、Redis 缓存、AOP 审计日志、定时任务、OSS 上传和 WebSocket 审核通知。

## 模块

- `media-common`: 统一返回、异常、JWT、OSS、AOP、上下文。
- `media-pojo`: Entity、DTO、VO。
- `media-server`: Controller、Service、Mapper、配置、任务、WebSocket。

## 启动

1. 使用 Java 21。当前机器如果 `JAVA_HOME` 仍指向 Java 8，可在 PowerShell 临时执行:

```powershell
$env:JAVA_HOME='F:\Javajdks\jdk21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

2. 创建数据库并导入。脚本会删除并重建 `news_db`:

```powershell
cmd /c "mysql -uroot -p000000 < sql\init.sql"
```

3. 修改配置:

`media-server/src/main/resources/application.yml`

4. 启动:

```powershell
.\mvnw.cmd -q -pl media-server -am install -DskipTests
.\mvnw.cmd -f media-server/pom.xml spring-boot:run
```

启动类: `com.connor.newsplatform.server.MediaNewsApplication`

## 测试账号

- 管理端: `admin / 123456`
- 管理端编辑: `editor / 123456`
- 用户端: `zhangsan / 123456`

## 验证

已使用 Java 21 执行:

```powershell
.\mvnw.cmd -q -pl media-server -am compile -DskipTests
```
