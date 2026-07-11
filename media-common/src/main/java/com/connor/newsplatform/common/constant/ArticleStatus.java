package com.connor.newsplatform.common.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 文章状态机（字符串存库，Navicat 直接可读）。
 * DRAFT 草稿 → PENDING 待审核 → APPROVED 审核通过 / REJECTED 审核驳回 → PUBLISHED 已发布 / OFFLINE 已下架
 */
public enum ArticleStatus {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    PUBLISHED,
    OFFLINE;

    /** Jackson 反序列化时兼容老的 Integer 数字（前端/历史数据） */
    @JsonCreator
    public static ArticleStatus from(Object value) {
        if (value == null) return null;
        if (value instanceof ArticleStatus s) return s;
        if (value instanceof Number n) {
            return switch (n.intValue()) {
                case 0 -> DRAFT;
                case 1 -> PENDING;
                case 2 -> APPROVED;
                case 3 -> REJECTED;
                case 4 -> PUBLISHED;
                case 5 -> OFFLINE;
                default -> throw new IllegalArgumentException("未知 ArticleStatus 数字：" + n);
            };
        }
        return ArticleStatus.valueOf(value.toString().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
