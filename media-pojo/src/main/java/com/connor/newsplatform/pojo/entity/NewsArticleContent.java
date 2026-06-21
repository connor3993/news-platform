package com.connor.newsplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("news_article_content")
public class NewsArticleContent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String content;
}
