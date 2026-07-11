package com.connor.newsplatform.common.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 付费订单状态机（字符串存库）。
 * CREATE 创建完成 → PAY_WAIT 等待支付 → PAY_SUCCESS 支付成功 / CLOSE 超时关单
 */
public enum OrderStatus {
    CREATE,
    PAY_WAIT,
    PAY_SUCCESS,
    CLOSE;

    @JsonCreator
    public static OrderStatus from(Object value) {
        if (value == null) return null;
        if (value instanceof OrderStatus s) return s;
        if (value instanceof Number n) {
            return switch (n.intValue()) {
                case 0 -> CREATE;
                case 1 -> PAY_WAIT;
                case 2 -> PAY_SUCCESS;
                case 3 -> CLOSE;
                default -> throw new IllegalArgumentException("未知 OrderStatus 数字：" + n);
            };
        }
        return OrderStatus.valueOf(value.toString().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
