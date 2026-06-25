package com.connor.newsplatform.server.service.impl;

import com.connor.newsplatform.common.constant.ArticleStatus;
import com.connor.newsplatform.common.constant.CacheKeys;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.entity.NewsArticle;
import com.connor.newsplatform.pojo.entity.NewsCategory;
import com.connor.newsplatform.pojo.entity.NewsDailyStats;
import com.connor.newsplatform.pojo.entity.SysOperationLog;
import com.connor.newsplatform.pojo.vo.ArticleVO;
import com.connor.newsplatform.pojo.vo.CategoryDistributionVO;
import com.connor.newsplatform.pojo.vo.DashboardTodayVO;
import com.connor.newsplatform.pojo.vo.TrendVO;
import com.connor.newsplatform.server.mapper.NewsArticleMapper;
import com.connor.newsplatform.server.mapper.NewsAuditRecordMapper;
import com.connor.newsplatform.server.mapper.NewsCategoryMapper;
import com.connor.newsplatform.server.mapper.NewsDailyStatsMapper;
import com.connor.newsplatform.server.mapper.NewsReadRecordMapper;
import com.connor.newsplatform.server.mapper.SysOperationLogMapper;
import com.connor.newsplatform.server.service.ArticleService;
import com.connor.newsplatform.server.service.StatisticsService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统计服务
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final NewsArticleMapper articleMapper;
    private final NewsAuditRecordMapper auditRecordMapper;
    private final NewsReadRecordMapper readRecordMapper;
    private final NewsDailyStatsMapper dailyStatsMapper;
    private final NewsCategoryMapper categoryMapper;
    private final SysOperationLogMapper logMapper;
    private final ArticleService articleService;
    private final RedisTemplate<String, Object> redisTemplate;

    public StatisticsServiceImpl(NewsArticleMapper articleMapper,
                                 NewsAuditRecordMapper auditRecordMapper,
                                 NewsReadRecordMapper readRecordMapper,
                                 NewsDailyStatsMapper dailyStatsMapper,
                                 NewsCategoryMapper categoryMapper,
                                 SysOperationLogMapper logMapper,
                                 ArticleService articleService,
                                 RedisTemplate<String, Object> redisTemplate) {
        this.articleMapper = articleMapper;
        this.auditRecordMapper = auditRecordMapper;
        this.readRecordMapper = readRecordMapper;
        this.dailyStatsMapper = dailyStatsMapper;
        this.categoryMapper = categoryMapper;
        this.logMapper = logMapper;
        this.articleService = articleService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 今日数据看板
     */
    @Override
    public DashboardTodayVO today() {
        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(CacheKeys.DASHBOARD_TODAY);
        if (cached instanceof DashboardTodayVO vo) {
            return vo;
        }
        // 2. 查数据库
        LocalDate today = LocalDate.now();
        LocalDateTime begin = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        DashboardTodayVO vo = new DashboardTodayVO();
        vo.setPublishCount(articleMapper.countByStatusAndPublishTime(ArticleStatus.PUBLISHED, begin, end));
        vo.setViewCount(readRecordMapper.countByDateRange(begin, end));
        vo.setAuditCount(auditRecordMapper.countByDateRange(begin, end));
        vo.setRejectCount(auditRecordMapper.countRejectedByDateRange(begin, end));

        // 3. 写缓存
        redisTemplate.opsForValue().set(CacheKeys.DASHBOARD_TODAY, vo, Duration.ofMinutes(5));
        return vo;
    }

    /**
     * 阅读趋势
     */
    @Override
    public List<TrendVO> readTrend(LocalDate begin, LocalDate end) {
        return statsBetween(begin, end).stream()
                .map(stats -> new TrendVO(stats.getStatDate().toString(), stats.getViewCount()))
                .collect(Collectors.toList());
    }

    /**
     * 发布趋势
     */
    @Override
    public List<TrendVO> publishTrend(LocalDate begin, LocalDate end) {
        return statsBetween(begin, end).stream()
                .map(stats -> new TrendVO(stats.getStatDate().toString(), stats.getPublishCount().longValue()))
                .collect(Collectors.toList());
    }

    /**
     * 热点top10
     */
    @Override
    public List<ArticleVO> hotTop10() {
        return articleService.hotList("hot");
    }

    /**
     * 分类分布
     */
    @Override
    public List<CategoryDistributionVO> categoryDistribution() {
        // 1. 查询所有已发布文章
        List<NewsArticle> articles = articleMapper.selectAllPublished();
        // 2. 按分类分组计数
        Map<Long, Long> countMap = articles.stream()
                .collect(Collectors.groupingBy(NewsArticle::getCategoryId, Collectors.counting()));
        // 3. 查询分类名称
        List<NewsCategory> categories = categoryMapper.selectByIds(new ArrayList<>(countMap.keySet()));
        Map<Long, NewsCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(NewsCategory::getId, Function.identity()));
        // 4. 组装结果
        return countMap.entrySet().stream()
                .map(entry -> new CategoryDistributionVO(
                        entry.getKey(),
                        categoryMap.containsKey(entry.getKey())
                                ? categoryMap.get(entry.getKey()).getName() : "未分类",
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 操作日志分页
     */
    @Override
    public PageResult<SysOperationLog> logPage(int page, int pageSize, String operation,
                                                LocalDateTime beginTime, LocalDateTime endTime) {
        PageHelper.startPage(page, pageSize);
        Page<SysOperationLog> p = logMapper.pageQuery(operation, beginTime, endTime);
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    /**
     * 生成每日统计（定时任务调用）
     */
    @Override
    @Transactional
    public void generateDailyStats(LocalDate date) {
        LocalDateTime begin = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        // 先删除旧数据
        dailyStatsMapper.deleteByDate(date);

        // 查询并统计
        NewsDailyStats stats = new NewsDailyStats();
        stats.setStatDate(date);
        stats.setArticleCount(articleMapper.countByCreateTime(begin, end));
        stats.setPublishCount(articleMapper.countByStatusAndPublishTime(ArticleStatus.PUBLISHED, begin, end));
        stats.setViewCount(readRecordMapper.countByDateRange(begin, end));
        stats.setAuditCount(auditRecordMapper.countByDateRange(begin, end));
        stats.setRejectCount(auditRecordMapper.countRejectedByDateRange(begin, end));
        stats.setCreateTime(LocalDateTime.now());

        dailyStatsMapper.insert(stats);
    }

    /**
     * 查询日期范围内的统计，缺失日期补0
     */
    private List<NewsDailyStats> statsBetween(LocalDate begin, LocalDate end) {
        LocalDate safeEnd = end == null ? LocalDate.now() : end;
        LocalDate safeBegin = begin == null ? safeEnd.minusDays(6) : begin;

        List<NewsDailyStats> existing = dailyStatsMapper.selectByDateRange(safeBegin, safeEnd);
        Map<LocalDate, NewsDailyStats> map = existing.stream()
                .collect(Collectors.toMap(NewsDailyStats::getStatDate, Function.identity()));

        List<NewsDailyStats> result = new ArrayList<>();
        for (LocalDate cursor = safeBegin; !cursor.isAfter(safeEnd); cursor = cursor.plusDays(1)) {
            result.add(map.getOrDefault(cursor, emptyStats(cursor)));
        }
        return result;
    }

    private NewsDailyStats emptyStats(LocalDate date) {
        NewsDailyStats stats = new NewsDailyStats();
        stats.setStatDate(date);
        stats.setArticleCount(0);
        stats.setPublishCount(0);
        stats.setViewCount(0L);
        stats.setAuditCount(0);
        stats.setRejectCount(0);
        return stats;
    }
}
