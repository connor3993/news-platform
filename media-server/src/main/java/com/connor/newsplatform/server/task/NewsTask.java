package com.connor.newsplatform.server.task;

import com.connor.newsplatform.common.constant.CacheKeys;
import com.connor.newsplatform.common.utils.DateTimeUtil;
import com.connor.newsplatform.server.service.ArticleService;
import com.connor.newsplatform.server.service.StatisticsService;
import com.connor.newsplatform.server.websocket.AuditWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class NewsTask {
    private final StatisticsService statisticsService;
    private final ArticleService articleService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditWebSocketHandler webSocketHandler;

    public NewsTask(StatisticsService statisticsService, ArticleService articleService, RedisTemplate<String, Object> redisTemplate,
                    AuditWebSocketHandler webSocketHandler) {
        this.statisticsService = statisticsService;
        this.articleService = articleService;
        this.redisTemplate = redisTemplate;
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(cron = "0 10 0 * * ?")
    public void generateYesterdayStats() {
        statisticsService.generateDailyStats(LocalDate.now().minusDays(1));
        log.info("daily stats generated");
        notifyAdmins("TASK_DAILY_STATS", "阅读统计已生成", "昨日阅读数据已统计完成，可在数据看板查看");
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshHotScore() {
        articleService.recalculateHotScore();
        articleService.hotList("hot");
        log.info("hot article cache refreshed");
        notifyAdmins("TASK_HOT_REFRESH", "资讯热度已刷新", "热点资讯排行和热度分已重新计算");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void clearExpiredHotCache() {
        redisTemplate.delete(CacheKeys.ARTICLE_HOT);
        log.info("hot article cache cleared");
        notifyAdmins("TASK_CACHE_CLEAR", "热点缓存已清理", "过期热点缓存已清理，后续访问会自动重建");
    }

    private void notifyAdmins(String type, String title, String content) {
        String message = "{\"type\":\"%s\",\"title\":\"%s\",\"content\":\"%s\",\"time\":\"%s\"}"
                .formatted(type, title, content, DateTimeUtil.format(java.time.LocalDateTime.now()));
        webSocketHandler.broadcastAdmins(message);
    }
}
