package com.connor.newsplatform.web.controller.admin;

import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.domain.vo.CategoryDistributionVO;
import com.connor.newsplatform.domain.vo.TrendSeriesVO;
import com.connor.newsplatform.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {
    private final StatisticsService statisticsService;

    public AdminStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/read")
    public Result<TrendSeriesVO> read(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(statisticsService.readTrend(begin, end));
    }

    @GetMapping("/publish")
    public Result<TrendSeriesVO> publish(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(statisticsService.publishTrend(begin, end));
    }

    @GetMapping("/category-distribution")
    public Result<List<CategoryDistributionVO>> categoryDistribution() {
        return Result.success(statisticsService.categoryDistribution());
    }
}
