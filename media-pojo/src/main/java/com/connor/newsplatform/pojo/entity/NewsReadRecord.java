package com.connor.newsplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("news_read_record")
public class NewsReadRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long articleId;
    private LocalDateTime createTime;
}
