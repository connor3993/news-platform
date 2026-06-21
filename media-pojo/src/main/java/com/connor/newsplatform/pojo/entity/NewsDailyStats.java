package com.connor.newsplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("news_daily_stats")
public class NewsDailyStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Integer articleCount;
    private Integer publishCount;
    private Long viewCount;
    private Integer auditCount;
    private Integer rejectCount;
    private LocalDateTime createTime;
}
