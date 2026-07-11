package com.connor.newsplatform.web.listener.pay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.connor.newsplatform.service.pay.PayRabbitMQConstants;
import com.connor.newsplatform.service.websocket.AuditWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 支付成功 MQ 监听器：支付成功后通知管理端，后续可扩展为发放权益、记账、通知等。
 */
@Slf4j
@Component
public class PaySuccessListener {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditWebSocketHandler webSocketHandler;

    public PaySuccessListener(AuditWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @RabbitListener(queues = PayRabbitMQConstants.PAY_SUCCESS_QUEUE)
    public void handleEvent(String paySuccessMessage) {
        JSONObject event = JSON.parseObject(paySuccessMessage);
        String orderId = event.getString("orderId");
        Long userId = event.getLong("userId");
        Long articleId = event.getLong("articleId");
        String articleTitle = event.getString("articleTitle");

        log.info("收到支付成功事件 orderId={} userId={} articleId={}",
                orderId, userId, articleId);

        // 通知所有管理员（dashboard 上能看到"今日营收"）
        String now = java.time.LocalDateTime.now().format(FMT);
        String adminMsg = String.format(
                "{\"type\":\"ORDER_PAID\",\"title\":\"收到一笔付费订单\",\"content\":\"文章《%s》收款成功\",\"time\":\"%s\"}",
                articleTitle, now);
        webSocketHandler.broadcastAdmins(adminMsg);
    }
}
