package com.connor.newsplatform.server.controller.admin;

import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.entity.SysOperationLog;
import com.connor.newsplatform.server.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/log")
public class AdminLogController {
    private final StatisticsService statisticsService;

    public AdminLogController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysOperationLog>> page(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    String operation,
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(statisticsService.logPage(page, pageSize, operation, beginTime, endTime));
    }
}
