package com.connor.newsplatform.domain.vo;

import lombok.Data;

@Data
public class DashboardTodayVO {
    private Integer publishCount;
    private Long viewCount;
    private Integer auditCount;
    private Integer rejectCount;
}
