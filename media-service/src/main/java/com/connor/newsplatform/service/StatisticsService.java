package com.connor.newsplatform.service;

import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.domain.po.SysOperationLog;
import com.connor.newsplatform.domain.vo.CategoryDistributionVO;
import com.connor.newsplatform.domain.vo.DashboardTodayVO;
import com.connor.newsplatform.domain.vo.TrendSeriesVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    DashboardTodayVO today();

    TrendSeriesVO readTrend(LocalDate begin, LocalDate end);

    TrendSeriesVO publishTrend(LocalDate begin, LocalDate end);

    List<CategoryDistributionVO> categoryDistribution();

    PageResult<SysOperationLog> logPage(int page, int pageSize, String operation, LocalDateTime beginTime, LocalDateTime endTime);

    void generateDailyStats(LocalDate date);
}
