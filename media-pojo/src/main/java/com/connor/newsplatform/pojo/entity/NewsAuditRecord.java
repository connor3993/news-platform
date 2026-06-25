package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsAuditRecord {
    private Long id;
    private Long articleId;
    private Long auditorId;
    private Integer auditStatus;
    private String auditComment;
    private LocalDateTime createTime;
}
