package com.connor.newsplatform.server.service;

import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.entity.SysOperationLog;
import com.connor.newsplatform.pojo.vo.ArticleVO;
import com.connor.newsplatform.pojo.vo.CategoryDistributionVO;
import com.connor.newsplatform.pojo.vo.DashboardTodayVO;
import com.connor.newsplatform.pojo.vo.TrendVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    DashboardTodayVO today();

    List<TrendVO> readTrend(LocalDate begin, LocalDate end);

    List<TrendVO> publishTrend(LocalDate begin, LocalDate end);

    List<ArticleVO> hotTop10();

    List<CategoryDistributionVO> categoryDistribution();

    PageResult<SysOperationLog> logPage(int page, int pageSize, String operation, LocalDateTime beginTime, LocalDateTime endTime);

    void generateDailyStats(LocalDate date);
}
