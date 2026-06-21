package com.connor.newsplatform.server.controller.admin;

import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.vo.ArticleVO;
import com.connor.newsplatform.pojo.vo.CategoryDistributionVO;
import com.connor.newsplatform.pojo.vo.TrendVO;
import com.connor.newsplatform.server.service.StatisticsService;
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
    public Result<List<TrendVO>> read(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(statisticsService.readTrend(begin, end));
    }

    @GetMapping("/publish")
    public Result<List<TrendVO>> publish(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(statisticsService.publishTrend(begin, end));
    }

    @GetMapping("/hot")
    public Result<List<ArticleVO>> hot() {
        return Result.success(statisticsService.hotTop10());
    }

    @GetMapping("/category-distribution")
    public Result<List<CategoryDistributionVO>> categoryDistribution() {
        return Result.success(statisticsService.categoryDistribution());
    }
}
