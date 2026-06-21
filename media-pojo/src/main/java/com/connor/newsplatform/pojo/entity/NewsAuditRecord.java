package com.connor.newsplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("news_audit_record")
public class NewsAuditRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long auditorId;
    private Integer auditStatus;
    private String auditComment;
    private LocalDateTime createTime;
}
