package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NewsArticle {
    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private Long categoryId;
    private Long authorId;
    private String authorType;
    private Integer status;
    private LocalDateTime publishTime;
    private Long viewCount;
    private Long likeCount;
    private Long dislikeCount;
    private Long favoriteCount;
    private Long commentCount;
    private BigDecimal hotScore;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
