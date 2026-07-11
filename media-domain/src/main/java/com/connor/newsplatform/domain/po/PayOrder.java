package com.connor.newsplatform.domain.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付费订单。
 * 状态机：0=创建完成 → 1=等待支付 → 2=支付成功 / 3=超时关单
 */
@Data
public class PayOrder {
    private Long id;
    private Long userId;
    private Long articleId;
    private String articleTitle;
    private String orderId;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private String status;
    private String payUrl;
    private LocalDateTime payTime;
    private String payTradeNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
