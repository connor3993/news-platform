package com.connor.newsplatform.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.connor.newsplatform.common.constant.ArticleStatus;
import com.connor.newsplatform.common.constant.CacheKeys;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.entity.NewsArticle;
import com.connor.newsplatform.pojo.entity.NewsAuditRecord;
import com.connor.newsplatform.pojo.entity.NewsCategory;
import com.connor.newsplatform.pojo.entity.NewsDailyStats;
import com.connor.newsplatform.pojo.entity.NewsReadRecord;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public DashboardTodayVO today() {
        Object cached = getCachedValue(CacheKeys.DASHBOARD_TODAY);
        if (cached instanceof DashboardTodayVO vo) {
            return vo;
        }
        LocalDate today = LocalDate.now();
        LocalDateTime begin = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        DashboardTodayVO vo = new DashboardTodayVO();
        vo.setPublishCount(articleMapper.selectCount(new LambdaQueryWrapper<NewsArticle>()
                .eq(NewsArticle::getStatus, ArticleStatus.PUBLISHED)
                .between(NewsArticle::getPublishTime, begin, end)).intValue());
        vo.setViewCount(readRecordMapper.selectCount(new LambdaQueryWrapper<NewsReadRecord>()
                .between(NewsReadRecord::getCreateTime, begin, end)));
        vo.setAuditCount(auditRecordMapper.selectCount(new LambdaQueryWrapper<NewsAuditRecord>()
                .between(NewsAuditRecord::getCreateTime, begin, end)).intValue());
        vo.setRejectCount(auditRecordMapper.selectCount(new LambdaQueryWrapper<NewsAuditRecord>()
                .eq(NewsAuditRecord::getAuditStatus, ArticleStatus.REJECTED)
                .between(NewsAuditRecord::getCreateTime, begin, end)).intValue());
        redisTemplate.opsForValue().set(CacheKeys.DASHBOARD_TODAY, vo, Duration.ofMinutes(5));
        return vo;
    }

    @Override
    public List<TrendVO> readTrend(LocalDate begin, LocalDate end) {
        return statsBetween(begin, end).stream()
                .map(stats -> new TrendVO(stats.getStatDate().toString(), stats.getViewCount()))
                .toList();
    }

    @Override
    public List<TrendVO> publishTrend(LocalDate begin, LocalDate end) {
        return statsBetween(begin, end).stream()
                .map(stats -> new TrendVO(stats.getStatDate().toString(), stats.getPublishCount().longValue()))
                .toList();
    }

    @Override
    public List<ArticleVO> hotTop10() {
        return articleService.hotList("hot");
    }

    @Override
    public List<CategoryDistributionVO> categoryDistribution() {
        List<NewsArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<NewsArticle>()
                .eq(NewsArticle::getStatus, ArticleStatus.PUBLISHED));
        Map<Long, Long> countMap = articles.stream()
                .collect(Collectors.groupingBy(NewsArticle::getCategoryId, Collectors.counting()));
        Map<Long, NewsCategory> categoryMap = categoryMapper.selectBatchIds(countMap.keySet())
                .stream()
                .collect(Collectors.toMap(NewsCategory::getId, Function.identity()));
        return countMap.entrySet().stream()
                .map(entry -> new CategoryDistributionVO(
                        entry.getKey(),
                        categoryMap.containsKey(entry.getKey()) ? categoryMap.get(entry.getKey()).getName() : "未分类",
                        entry.getValue()))
                .toList();
    }

    @Override
    public PageResult<SysOperationLog> logPage(int page, int pageSize, String operation, LocalDateTime beginTime, LocalDateTime endTime) {
        Page<SysOperationLog> result = logMapper.selectPage(Page.of(page, pageSize), new LambdaQueryWrapper<SysOperationLog>()
                .like(StringUtils.hasText(operation), SysOperationLog::getOperation, operation)
                .ge(beginTime != null, SysOperationLog::getCreateTime, beginTime)
                .le(endTime != null, SysOperationLog::getCreateTime, endTime)
                .orderByDesc(SysOperationLog::getCreateTime));
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public void generateDailyStats(LocalDate date) {
        LocalDateTime begin = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        dailyStatsMapper.delete(new LambdaQueryWrapper<NewsDailyStats>().eq(NewsDailyStats::getStatDate, date));
        NewsDailyStats stats = new NewsDailyStats();
        stats.setStatDate(date);
        stats.setArticleCount(articleMapper.selectCount(new LambdaQueryWrapper<NewsArticle>()
                .between(NewsArticle::getCreateTime, begin, end)).intValue());
        stats.setPublishCount(articleMapper.selectCount(new LambdaQueryWrapper<NewsArticle>()
                .eq(NewsArticle::getStatus, ArticleStatus.PUBLISHED)
                .between(NewsArticle::getPublishTime, begin, end)).intValue());
        stats.setViewCount(readRecordMapper.selectCount(new LambdaQueryWrapper<NewsReadRecord>()
                .between(NewsReadRecord::getCreateTime, begin, end)));
        stats.setAuditCount(auditRecordMapper.selectCount(new LambdaQueryWrapper<NewsAuditRecord>()
                .between(NewsAuditRecord::getCreateTime, begin, end)).intValue());
        stats.setRejectCount(auditRecordMapper.selectCount(new LambdaQueryWrapper<NewsAuditRecord>()
                .eq(NewsAuditRecord::getAuditStatus, ArticleStatus.REJECTED)
                .between(NewsAuditRecord::getCreateTime, begin, end)).intValue());
        stats.setCreateTime(LocalDateTime.now());
        dailyStatsMapper.insert(stats);
    }

    private List<NewsDailyStats> statsBetween(LocalDate begin, LocalDate end) {
        LocalDate safeEnd = end == null ? LocalDate.now() : end;
        LocalDate safeBegin = begin == null ? safeEnd.minusDays(6) : begin;
        List<NewsDailyStats> existing = dailyStatsMapper.selectList(new LambdaQueryWrapper<NewsDailyStats>()
                .between(NewsDailyStats::getStatDate, safeBegin, safeEnd)
                .orderByAsc(NewsDailyStats::getStatDate));
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

    private Object getCachedValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RuntimeException ex) {
            redisTemplate.delete(key);
            return null;
        }
    }
}
