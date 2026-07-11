package com.connor.newsplatform.web.job.pay;

import com.connor.newsplatform.service.pay.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 超时关单定时任务（每 10 分钟扫描一次）。
 * 对应 s-pay-mall-moni 的 TimeoutCloseOrderJob。
 */
@Slf4j
@Component
public class TimeoutCloseOrderJob {

    private final OrderService orderService;

    public TimeoutCloseOrderJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            List<String> orderIds = orderService.queryTimeoutCloseList();
            if (orderIds == null || orderIds.isEmpty()) {
                return;
            }
            for (String orderId : orderIds) {
                boolean ok = orderService.changeOrderClose(orderId);
                log.info("超时关单 orderId={} ok={}", orderId, ok);
            }
        } catch (Exception e) {
            log.error("超时关单任务失败", e);
        }
    }
}
