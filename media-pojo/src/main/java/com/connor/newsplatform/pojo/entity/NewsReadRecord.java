package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsReadRecord {
    private Long id;
    private Long userId;
    private Long articleId;
    private LocalDateTime createTime;
}
