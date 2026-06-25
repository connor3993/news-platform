package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsArticleComment {
    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
}
