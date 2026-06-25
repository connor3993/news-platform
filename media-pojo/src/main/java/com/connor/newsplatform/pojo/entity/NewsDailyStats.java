package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class NewsDailyStats {
    private Long id;
    private LocalDate statDate;
    private Integer articleCount;
    private Integer publishCount;
    private Long viewCount;
    private Integer auditCount;
    private Integer rejectCount;
    private LocalDateTime createTime;
}
