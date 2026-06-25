package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysOperationLog {
    private Long id;
    private Long operatorId;
    private String operatorType;
    private String operation;
    private String requestUri;
    private String requestMethod;
    private String requestParams;
    private String ip;
    private LocalDateTime createTime;
}
