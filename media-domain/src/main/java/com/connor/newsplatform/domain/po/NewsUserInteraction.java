package com.connor.newsplatform.domain.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户互动（合并原 news_article_vote / news_article_favorite，通过 action_type 区分 like / dislike / favorite）
 */
@Data
public class NewsUserInteraction {
    private Long id;
    private Long userId;
    private Long articleId;
    private String actionType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
