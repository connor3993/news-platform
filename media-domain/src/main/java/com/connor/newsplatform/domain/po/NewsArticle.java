package com.connor.newsplatform.domain.po;

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
    private String status;
    private LocalDateTime publishTime;
    private Long viewCount;
    private Long likeCount;
    private Long dislikeCount;
    private Long favoriteCount;
    private String rejectReason;
    private String content;
    private Integer isPaid;
    private BigDecimal price;
    private String paidPreview;
    private String paidOnly;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
