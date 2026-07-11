# 项目长期记忆 (newsPlatform)

## 技术栈速记
- 后端 Spring Boot 3.5.3 + Java 21（README 写的 4.x 是笔误）、MyBatis + PageHelper、JWT 双密钥、Redis、OSS、WebSocket。
- 用户端前端：vite + vue3，已精简为 6 个依赖，黑线框风格，dev 端口 5174，代理到 8080。

## 后端构建与运行（重要，2026-07-10 验证）
- **构建**：系统 `mvnw` 与旧 `/tmp/mvn3916` 解压包均不可用（classworlds 为空/路径转换坏）。用 PowerShell 调原生 `E:\apache-maven-3.9.4\bin\mvn.cmd`（Git Bash 下 `/e/...` 会被错误转义成 `\e\...`）。命令：`& "E:\apache-maven-3.9.4\bin\mvn.cmd" -o -pl media-web -am package -DskipTests -f "F:\JavaWebProjects\newsPlatform\pom.xml"`。
- **重打包前必须先停后端**：否则 `spring-boot:repackage` 因旧 jar 被 java 进程锁住无法改名 → BUILD FAILURE。
- **运行**：`env -u SERVER_PORT F:\Javajdks\jdk21\bin\java.exe -jar media-web/target/media-web-0.0.1-SNAPSHOT.jar --server.port=8080`（必须解绑 SERVER_PORT，否则被 WorkBuddy 运行时占 49032）。
- 依赖已缓存于 `C:\Users\mol\.m2`，可离线 `-o` 构建。

## codegraph 使用
- CLI 路径 `C:\Users\mol\AppData\Local\codegraph\current\bin\codegraph.cmd`
- `cmd //c "codegraph.cmd init ..."` 会吞输出且不建索引；**必须用 PowerShell** 调 `& $bin init "F:\JavaWebProjects\newsPlatform"`。
- 已索引：168 files / 2713 nodes / 5165 edges（后端 94 java + 前端 61 vue/js/ts + 配置）。随时可用 `codegraph_explore`。
