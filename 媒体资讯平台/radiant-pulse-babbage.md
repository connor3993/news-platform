# newsPlatform 功能增强计划：ES + Kafka + Redis防护 + Redisson

## 定位
面试展示项目，重点是"技术选用 + 产品可行性 + 稳定可运行"。弃用推荐算法，聚焦中间件含金量。

## 现状
- Spring Boot 3.5.3 + 纯MyBatis(XML) + Redis + WebSocket
- 已有：审核状态机、热度分、Redis基础缓存(分类列表/今日看板)、AOP(@AutoFill/@LogRecord)、定时任务
- 缓存现状：固定TTL，无防护；行为同步写库；搜索用MySQL LIKE

## 7个功能模块

| #   | 模块             | 技术                                                 | 面试亮点                 |
| --- | -------------- | -------------------------------------------------- | -------------------- |
| 1   | ES全文搜索         | ES multi_match + highlight + 聚合                    | 倒排索引 vs B+Tree、分词、高亮 |
| 2   | ES搜索联想词        | ES completion suggester (FST)                      | 前缀查询O(1)、上下文过滤       |
| 3   | 稿件状态同步ES       | Kafka解耦 + 事务后发送 + _id幂等                            | 最终一致、重试+死信队列         |
| 4   | 行为日志异步采集       | Kafka削峰 + 批量落库 + Redis INCR                        | 削峰填谷、批量写             |
| 5   | **文章详情缓存三重防护** | RLock防击穿 + RBloomFilter防穿透 + 随机TTL防雪崩 + Caffeine多级 | **一个模块讲透三大经典问题**     |
| 6   | 接口限流防刷         | RRateLimiter令牌桶 + AOP @RateLimit                   | 令牌桶vs漏桶、Lua原子性、分布式限流 |
| 7   | 防重复提交          | RLock分布式锁 + AOP @Idempotent + SpEL                 | 看门狗续期、与本地token防重区别   |


### 模块1：ES全文搜索
- **描述**：替换 `userPageQuery` 的 MySQL LIKE，支持标题+摘要 multi_match + 高亮 + 分类聚合
- **原理**：IK分词 + bool query + highlight(`<em>`) + terms aggregation
- **ES索引**：`news_article_doc`（id, title[text ik_max_word], summary[text], content[text], categoryId[keyword], status[integer], publishTime[date], hotScore[double]）
- **方法**：`SearchService.search(SearchDTO): PageResult<ArticleSearchVO>`
- **集成**：`UserArticleController#page` 加 `from=es` 参数分流
- **面试亮点**：倒排索引 vs B+Tree、multi_match多字段权重、highlight、聚合分桶

### 模块2：ES搜索联想词
- **描述**：输入框实时联想，用 completion suggester 前缀匹配
- **原理**：completion字段用FST内存结构，前缀查询O(1)
- **ES索引**：`news_article_suggest`（titleSuggest[completion with context categoryId]）
- **方法**：`SearchService.suggest(String prefix, Long categoryId): List<String>`
- **集成**：新增 `GET /user/article/suggest`
- **面试亮点**：completion的FST原理、与match phrase区别、上下文过滤

### 模块3：稿件状态变更同步ES（Kafka解耦）
- **描述**：稿件发布/下架/删除时发Kafka异步同步ES，避免主流程阻塞
- **原理**：Producer在事务提交后发消息(`TransactionSynchronizationManager`)，Consumer用articleId作ES `_id` 天然幂等
- **Topic**：`news-article-sync`，分区3，消息体 `{"id":1,"op":"PUBLISH|OFFLINE|DELETE|UPDATE"}`
- **方法**：`ArticleSyncProducer.send(Long id, String op)` / `ArticleSyncConsumer.onMessage(...)`
- **集成**：`ArticleServiceImpl#publish/offline/deleteArticle/updateArticle` 末尾调用producer
- **面试亮点**：事务后发送防消息丢、`_id`幂等、消费失败重试+死信队列

### 模块4：用户行为日志异步采集（Kafka削峰）
- **描述**：阅读/点赞/评论行为发Kafka，消费者批量聚合写库保护MySQL
- **原理**：高QPS行为入Kafka削峰，消费者批量insertBatch + 定时刷盘
- **新表**：`news_behavior_log(id, userId, articleId, type, createTime)`
- **Topic**：`news-behavior-log`，按userId取模分区保证同用户有序
- **方法**：`BehaviorLogProducer.send(BehaviorLogDTO)` / `BehaviorLogConsumer.flush()`
- **集成**：`ArticleServiceImpl#recordRead/vote/addComment` 同步写改为发消息；点赞计数改Redis INCR异步落库
- **面试亮点**：削峰填谷、批量写、Redis INCR + 定时落库经典组合

### 模块5：文章详情缓存三重防护（核心）
- **描述**：文章详情缓存同时防雪崩+击穿+穿透，一个模块讲透三大经典问题
- **原理**：
  - **雪崩**：TTL = `30min + random(0~5min)`；Caffeine一级缓存 + Redis二级
  - **击穿**：缓存miss时 `RLock.tryLock`，只允许一个线程回源，其余自旋重试
  - **穿透**：启动时 `RBloomFilter` 装载所有已发布articleId；查前contains判断；DB为空缓存空值`""`短TTL(2min)
- **Redis Key**：`media:article:detail:{id}`(值) / `media:article:lock:{id}`(锁) / `media:article:null:{id}`(空值)
- **方法**：`ArticleCacheService.getDetail(Long id): ArticleVO` / `BloomFilterInitRunner`(启动装载)
- **集成**：`ArticleServiceImpl#userDetail` 改调 `ArticleCacheService.getDetail`；发布时 `bloomFilter.add(id)`
- **面试亮点**：三大经典问题+三种解法，Redisson RLock + RBloomFilter 一次用全

### 模块6：接口限流防刷（Redisson RRateLimiter + AOP）
- **描述**：用户端高频接口按用户/IP限流，防爬虫刷量
- **原理**：Redisson `RRateLimiter` 基于Redis+Lua令牌桶，分布式一致；AOP注解 `@RateLimit` 声明式
- **注解**：`@RateLimit(rate=10, interval=1, key="userId")`
- **方法**：`RateLimitAspect`(Around) / `RateLimitService.tryAcquire(key, rate, interval): boolean`
- **集成**：`UserArticleController#comment/like/page` 加 `@RateLimit`；`SearchService#search` 按IP限流
- **面试亮点**：令牌桶vs漏桶、Lua原子性、AOP声明式、分布式限流与单机Guava取舍

### 模块7：防重复提交（Redisson分布式锁AOP）
- **描述**：用户投稿、评论等写操作防重复提交（前端连点、网络重试）
- **原理**：`RLock` 以 `userId+接口+参数hash` 为key，TTL 5s，未获锁直接拒绝
- **注解**：`@Idempotent(expire=5, key="'submit:'+ #dto.title")`（SpEL）
- **方法**：`IdempotentAspect`(Around)
- **集成**：`UserArticleController#submitArticle/comment` 加 `@Idempotent`
- **面试亮点**：SpEL解析key、锁自动续期看门狗、与本地token防重区别

## 实施顺序
```
1. 依赖+配置（pom + yml + RedissonConfig + KafkaConfig + EsConfig + CaffeineConfig）
   ↓
2. 模块7(防重复提交) + 模块6(限流)  ← 纯Redisson，先跑通验证Redisson
   ↓
3. 模块5(缓存防护)  ← 依赖Redisson RLock/RBloomFilter
   ↓
4. 模块3(Kafka同步ES)  ← Kafka+ES一起搭
   ↓
5. 模块1+模块2(ES搜索+联想)  ← 依赖模块3的数据
   ↓
6. 模块4(行为日志异步)  ← 改造现有写操作，放最后避免影响主流程
```

## 关键技术难点
1. **ES与MySQL一致性**：Kafka异步+重试+死信，接受最终一致；对账定时任务校验
2. **布隆过滤器误判**：只能假阳不能假阴，删除困难→用counting bloom或重启重载
3. **RLock看门狗**：默认30s续期，业务超时要小心；`tryLock(waitTime, leaseTime, unit)`显式指定更可控
4. **Kafka事务消息**：MySQL事务+Kafka发送需用 `TransactionSynchronizationManager.registerSynchronization` 在afterCommit发
5. **Caffeine多级缓存一致性**：Redis失效时同步清Caffeine，或用短TTL(30s)容忍短暂不一致
6. **RedisTemplate序列化**：现有配置激活了DefaultTyping，空值缓存用`""`而非null

## 要修改的文件清单

### 配置（修改）
- `pom.xml`（父pom加ES/Kafka/Redisson/Caffeine版本管理）
- `media-server/pom.xml`（加具体依赖）
- `media-server/src/main/resources/application.yml`（加es/kafka/redisson配置）
- 新增 `media-server/.../config/RedissonConfig.java`
- 新增 `media-server/.../config/EsConfig.java`
- 新增 `media-server/.../config/KafkaConfig.java`
- 新增 `media-server/.../config/CaffeineConfig.java`

### 缓存与常量（修改+新增）
- 修改 `media-common/.../constant/CacheKeys.java`（加detail/lock/null/suggest key前缀）
- 新增 `media-common/.../annotation/RateLimit.java`
- 新增 `media-common/.../annotation/Idempotent.java`

### Service/Controller（修改）
- 修改 `media-server/.../service/impl/ArticleServiceImpl.java`（userDetail接缓存、recordRead/vote/addComment接Kafka、publish/offline/delete发同步消息）
- 修改 `media-server/.../controller/user/UserArticleController.java`（加@RateLimit/@Idempotent、新增search/suggest接口）
- 修改 `media-server/.../task/NewsTask.java`（加布隆过滤器重载、ES对账任务）

### 新增Service/Mapper/Aspect
- 新增 `media-server/.../service/SearchService.java` + impl
- 新增 `media-server/.../service/ArticleCacheService.java` + impl
- 新增 `media-server/.../service/ArticleSyncProducer.java`、`ArticleSyncConsumer.java`
- 新增 `media-server/.../service/BehaviorLogProducer.java`、`BehaviorLogConsumer.java`
- 新增 `media-common/.../aspect/RateLimitAspect.java`、`IdempotentAspect.java`
- 新增 `media-server/.../runner/BloomFilterInitRunner.java`
- 新增 `media-server/.../mapper/NewsBehaviorLogMapper.java` + XML
- 追加 `sql/init.sql` 的 `news_behavior_log` 表DDL

### POJO（新增）
- 新增 `media-pojo/.../dto/SearchDTO.java`、`BehaviorLogDTO.java`、`ArticleSyncMsg.java`
- 新增 `media-pojo/.../vo/ArticleSearchVO.java`

## 面试讲解主线
"缓存三大问题一个模块讲透(RLock+RBloomFilter) → 分布式锁还能做防重复提交 → 限流用RRateLimiter+AOP → 搜索从LIKE升级到ES(倒排索引+分词+高亮) → 写库和写ES用Kafka解耦(削峰+最终一致) → 高频行为日志也走Kafka批量落库"
