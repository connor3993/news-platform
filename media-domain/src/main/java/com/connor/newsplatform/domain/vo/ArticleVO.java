package com.connor.newsplatform.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private String status;
    private String content;
    private LocalDateTime publishTime;
    private Long viewCount;
    private Long likeCount;
    private Long dislikeCount;
    private Long favoriteCount;
    private Integer userVote;
    private Boolean favorited;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    // 知识付费字段
    private Integer isPaid;
    private BigDecimal price;
    /** 当前用户是否已购买（仅 isPaid=1 时有效） */
    private Boolean purchased;
    /** 已购买或免费文章返回 paidOnly；未购买时为空 */
    private String paidOnly;
    /** 付费预览（已购买或免费文章为空，付费墙渲染用） */
    private String paidPreview;
}
