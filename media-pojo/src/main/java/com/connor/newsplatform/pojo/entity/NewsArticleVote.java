package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsArticleVote {
    private Long id;
    private Long userId;
    private Long articleId;
    private Integer voteType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
