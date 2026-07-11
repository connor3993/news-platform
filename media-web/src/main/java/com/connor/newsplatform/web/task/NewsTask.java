package com.connor.newsplatform.web.task;

import com.connor.newsplatform.common.utils.DateTimeUtil;
import com.connor.newsplatform.service.ArticleService;
import com.connor.newsplatform.service.StatisticsService;
import com.connor.newsplatform.service.websocket.AuditWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class NewsTask {
    private final StatisticsService statisticsService;
    private final ArticleService articleService;
    private final AuditWebSocketHandler webSocketHandler;

    public NewsTask(StatisticsService statisticsService, ArticleService articleService,
                    AuditWebSocketHandler webSocketHandler) {
        this.statisticsService = statisticsService;
        this.articleService = articleService;
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(cron = "0 10 0 * * ?")
    public void generateYesterdayStats() {
        statisticsService.generateDailyStats(LocalDate.now().minusDays(1));
        log.info("daily stats generated");
        notifyAdmins("TASK_DAILY_STATS", "阅读统计已生成", "昨日阅读数据已统计完成，可在数据看板查看");
    }

    private void notifyAdmins(String type, String title, String content) {
        String message = "{\"type\":\"%s\",\"title\":\"%s\",\"content\":\"%s\",\"time\":\"%s\"}"
                .formatted(type, title, content, DateTimeUtil.format(java.time.LocalDateTime.now()));
        webSocketHandler.broadcastAdmins(message);
    }
}
