package com.connor.newsplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("news_article")
public class NewsArticle {
    @TableId(type = IdType.AUTO)
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
