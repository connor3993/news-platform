package com.connor.newsplatform.domain.res;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付订单创建结果。
 * payUrl 含义：mock 模式 = 平台内"模拟支付"跳转地址；真沙箱模式 = 支付宝收银台表单 HTML。
 */
@Data
@Builder
public class PayOrderRes {
    private String orderId;
    private String payUrl;
    private BigDecimal totalAmount;
    private String status;
}
