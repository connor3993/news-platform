## WebSocket 实时消息推送工作流程

本项目使用 Spring WebSocket 实现后端向前端实时推送消息，主要用于稿件审核状态变更通知和定时任务执行通知。管理端通过 WebSocket 连接接收通知，无需轮询或刷新页面。

---

### 整体架构

```
┌──────────────────┐         ┌──────────────────────┐         ┌──────────────────┐
│   管理端前端      │  WebSocket │    Spring Boot 后端    │  业务触发  │   Service / Task   │
│                  │◄────────►│                      │◄────────│                  │
│  WebSocketStore  │  长连接   │ AuditWebSocketHandler│         │ ArticleService   │
│  wsService.js    │          │ (ConcurrentHashMap)  │         │ NewsTask         │
└──────────────────┘          └──────────────────────┘         └──────────────────┘

端点:
  /ws/audit/{adminId}                    ← 管理员专用（前端使用此端点）
  /ws/notify/{receiverType}/{receiverId} ← 通用端点（支持 admin / user）
```

---

### 第一步：WebSocket 配置与端点注册

`WebSocketConfig` 注册了两个 WebSocket 端点，并通过握手拦截器从 URL 路径中提取用户身份信息。

**端点 1：/ws/audit/{adminId}**

```java
registry.addHandler(auditWebSocketHandler, "/ws/audit/{adminId}")
    .addInterceptors(new HandshakeInterceptor() {
        @Override
        public boolean beforeHandshake(..., Map<String, Object> attributes) {
            // 从 URL 路径提取 adminId
            String path = request.getURI().getPath();
            String adminId = path.substring(path.lastIndexOf('/') + 1);
            attributes.put("adminId", Long.valueOf(adminId));
            attributes.put("receiverType", "admin");
            attributes.put("receiverId", Long.valueOf(adminId));
            return true;  // 允许握手
        }
    })
    .setAllowedOriginPatterns("*");  // 允许跨域
```

**端点 2：/ws/notify/{receiverType}/{receiverId}**

```java
registry.addHandler(auditWebSocketHandler, "/ws/notify/{receiverType}/{receiverId}")
    .addInterceptors(new HandshakeInterceptor() {
        @Override
        public boolean beforeHandshake(..., Map<String, Object> attributes) {
            // 从 URL 路径提取 receiverType 和 receiverId
            String receiverType = parts[parts.length - 2];  // "admin" 或 "user"
            String receiverId = parts[parts.length - 1];
            if (!"admin".equals(receiverType) && !"user".equals(receiverType)) {
                return false;  // 拒绝非法类型
            }
            attributes.put("receiverType", receiverType);
            attributes.put("receiverId", Long.valueOf(receiverId));
            return true;
        }
    });
```

---

### 第二步：连接管理与消息发送

`AuditWebSocketHandler` 是核心处理器，维护两个 `ConcurrentHashMap` 分别存储管理员和用户的 WebSocket 会话。

**会话管理：**

```java
@Component
public class AuditWebSocketHandler extends TextWebSocketHandler {

    // 管理员会话池: adminId → WebSocketSession
    private final Map<Long, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

    // 用户会话池: userId → WebSocketSession
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // 连接建立时：将 session 存入对应的 Map
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String receiverType = extractReceiverType(session);
        Long receiverId = extractReceiverId(session);
        if ("admin".equals(receiverType)) {
            adminSessions.put(receiverId, session);
        } else if ("user".equals(receiverType)) {
            userSessions.put(receiverId, session);
        }
    }

    // 连接关闭时：从 Map 中移除
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 对应的 remove 操作...
    }
}
```

**消息发送方法：**

```java
// 向指定管理员发送消息
public void sendToAdmin(Long adminId, String message) {
    WebSocketSession session = adminSessions.get(adminId);
    if (session != null && session.isOpen()) {
        session.sendMessage(new TextMessage(message));
    }
}

// 向指定用户发送消息
public void sendToUser(Long userId, String message) { ... }

// 广播给所有在线管理员
public void broadcastAdmins(String message) {
    adminSessions.forEach((adminId, session) -> sendToAdmin(adminId, message));
}

// 广播给所有人（当前实现等同 broadcastAdmins）
public void broadcast(String message) {
    broadcastAdmins(message);
}
```

---

### 第三步：前端建立 WebSocket 连接

管理端前端在**登录成功后**建立 WebSocket 连接。

**前端 WebSocket 服务（utils/websocket.js）：**

```javascript
class WebSocketService {
    connect(adminId) {
        this.adminId = adminId;
        this.reconnectCount = 0;
        this._doConnect();
    }

    _doConnect() {
        const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
        const url = `${protocol}//${location.host}/ws/audit/${this.adminId}`;
        this.ws = new WebSocket(url);

        this.ws.onopen = () => {
            this.reconnectCount = 0;       // 连接成功，重置重连计数
            this.onConnect?.();            // 通知 Store 更新状态
        };

        this.ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            if (data.type === 'ARTICLE_AUDIT') {
                // 弹出 Element Plus 右上角通知
                ElNotification({
                    title: data.title,
                    message: data.content,
                    type: 'info',
                    duration: 5000
                });
            }
            this.onMessage?.(data);       // 通知 Store 存入消息列表
        };

        this.ws.onclose = () => {
            this.onDisconnect?.();
            this._reconnect();            // 自动重连
        };
    }

    _reconnect() {
        if (this.reconnectCount >= 5) return;  // 最多重连 5 次
        this.reconnectCount++;
        setTimeout(() => this._doConnect(), 3000);  // 3 秒后重连
    }

    disconnect() {
        this.reconnectCount = 5;  // 阻止重连
        this.ws?.close();
    }
}
```

**前端 Pinia Store（stores/websocket.js）：**

```javascript
export const useWebSocketStore = defineStore('websocket', () => {
    const connected = ref(false);
    const messageList = ref([]);
    const reconnectCount = ref(0);

    function connect(adminId) {
        wsService.onConnect = () => { connected.value = true; };
        wsService.onDisconnect = () => { connected.value = false; };
        wsService.onMessage = (data) => { pushMessage(data); };
        wsService.connect(adminId);
    }

    function disconnect() {
        wsService.disconnect();
        connected.value = false;
        messageList.value = [];
    }

    function pushMessage(msg) {
        messageList.value.unshift({ ...msg, time: msg.time || new Date().toLocaleString() });
        if (messageList.value.length > 50) messageList.value.pop();
    }

    return { connected, messageList, reconnectCount, connect, disconnect, pushMessage };
});
```

**登录时触发连接（views/Login.vue）：**

```javascript
async function handleLogin() {
    const data = await adminStore.login(loginForm);
    // 登录成功后建立 WebSocket
    const wsStore = useWebSocketStore();
    wsStore.connect(data.id);
    router.push('/dashboard');
}
```

**退出时断开连接：**

```javascript
function handleLogout() {
    const wsStore = useWebSocketStore();
    wsStore.disconnect();           // 关闭 WebSocket
    adminStore.logout();            // 清除登录状态
    router.push('/login');
}
```

---

### 第四步：后端触发消息推送

有两种场景会触发 WebSocket 消息推送：

#### 场景 A：审核操作后推送

当管理员审核一篇文章（通过或驳回）时，`ArticleService` 完成数据库操作后调用 `AuditWebSocketHandler` 推送通知。

```
管理员A 审核通过文章
  │
  ArticleService.approve(articleId, auditComment)
  │  ├── 更新 news_article.status = 2
  │  ├── 插入 news_audit_record
  │  └── webSocketHandler.broadcastAdmins(message)
  │
  所有在线管理员的浏览器右上角弹出通知：
  "稿件审核状态更新：稿件《AI 技术带来媒体生产新变化》已审核通过"
```

#### 场景 B：定时任务执行后推送

`NewsTask` 中有三个定时任务，每次执行完毕后都会通过 WebSocket 通知管理员。

```java
@Component
public class NewsTask {

    // 每天 00:10 生成昨日统计
    @Scheduled(cron = "0 10 0 * * ?")
    public void generateYesterdayStats() {
        statisticsService.generateDailyStats(LocalDate.now().minusDays(1));
        notifyAdmins("TASK_DAILY_STATS", "阅读统计已生成",
                     "昨日阅读数据已统计完成，可在数据看板查看");
    }

    // 每 10 分钟刷新热度
    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshHotScore() {
        articleService.recalculateHotScore();
        articleService.hotList("hot");
        notifyAdmins("TASK_HOT_REFRESH", "资讯热度已刷新",
                     "热点资讯排行和热度分已重新计算");
    }

    // 每天 02:00 清理过期热点缓存
    @Scheduled(cron = "0 0 2 * * ?")
    public void clearExpiredHotCache() {
        redisTemplate.delete(CacheKeys.ARTICLE_HOT);
        notifyAdmins("TASK_CACHE_CLEAR", "热点缓存已清理",
                     "过期热点缓存已清理，后续访问会自动重建");
    }

    private void notifyAdmins(String type, String title, String content) {
        String message = """
            {"type":"%s","title":"%s","content":"%s","time":"%s"}
            """.formatted(type, title, content, DateTimeUtil.format(LocalDateTime.now()));
        webSocketHandler.broadcastAdmins(message);
    }
}
```

---

### 消息格式

所有 WebSocket 消息统一使用 JSON 格式：

```json
{
    "type": "ARTICLE_AUDIT",
    "title": "稿件审核状态更新",
    "content": "稿件《AI 技术带来媒体生产新变化》已审核通过",
    "time": "2026-06-21 10:00:00"
}
```

消息类型（type 字段）：

| type | 触发场景 | 说明 |
|------|----------|------|
| ARTICLE_AUDIT | 管理员审核稿件 | 审核通过或驳回时推送 |
| TASK_DAILY_STATS | 每日统计定时任务 | 凌晨 00:10 执行完毕后推送 |
| TASK_HOT_REFRESH | 热度刷新定时任务 | 每 10 分钟执行完毕后推送 |
| TASK_CACHE_CLEAR | 缓存清理定时任务 | 凌晨 02:00 执行完毕后推送 |

---

### 完整生命周期

```
1. 管理员登录
   └── 前端调用 ws://localhost:8080/ws/audit/{adminId}
       └── 后端 HandshakeInterceptor 提取 adminId，存入 session attributes
           └── AuditWebSocketHandler.afterConnectionEstablished()
               └── adminSessions.put(adminId, session)

2. 业务事件触发推送
   └── ArticleService / NewsTask 调用 broadcastAdmins(message)
       └── 遍历 adminSessions，逐个 sendMessage
           └── 前端 ws.onmessage 接收
               └── ElNotification 弹出右上角通知
               └── Store.pushMessage 存入消息列表

3. 连接断开
   └── 前端 ws.onclose 触发
       └── 3 秒后自动重连（最多 5 次）
           └── 重连成功 → reconnectCount 归零
           └── 重连失败 → 继续尝试，直到达到上限

4. 管理员退出登录
   └── 前端 wsService.disconnect()
       └── reconnectCount 设为 5（阻止重连）
       └── ws.close()
           └── 后端 afterConnectionClosed()
               └── adminSessions.remove(adminId)
```

---

### 设计要点

1. **ConcurrentHashMap 保证线程安全**：多个管理员同时连接/断开时不会并发修改异常
2. **广播模式**：审核操作和定时任务通知发送给所有在线管理员，而不是只发给操作人
3. **自动重连机制**：前端断开后自动重连，最多 5 次，每次间隔 3 秒，避免网络抖动导致永久断连
4. **优雅断开**：退出登录时主动设置 `reconnectCount = 5` 阻止重连，避免退出后还在不断尝试连接
5. **Vite 代理**：开发环境的 `vite.config.js` 配置了 `/ws` 路径的 WebSocket 代理到后端 8080 端口
