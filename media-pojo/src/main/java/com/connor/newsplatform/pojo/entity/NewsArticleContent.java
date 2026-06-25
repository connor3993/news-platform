package com.connor.newsplatform.pojo.entity;

import lombok.Data;

@Data
public class NewsArticleContent {
    private Long id;
    private Long articleId;
    private String content;
}
