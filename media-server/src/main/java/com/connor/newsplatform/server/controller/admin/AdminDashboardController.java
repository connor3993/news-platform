package com.connor.newsplatform.server.controller.admin;

import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.vo.DashboardTodayVO;
import com.connor.newsplatform.server.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {
    private final StatisticsService statisticsService;

    public AdminDashboardController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/today")
    public Result<DashboardTodayVO> today() {
        return Result.success(statisticsService.today());
    }
}
