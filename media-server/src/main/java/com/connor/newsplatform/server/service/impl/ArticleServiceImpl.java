package com.connor.newsplatform.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.connor.newsplatform.common.constant.ArticleStatus;
import com.connor.newsplatform.common.constant.CacheKeys;
import com.connor.newsplatform.common.constant.JwtClaimsConstant;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.common.utils.DateTimeUtil;
import com.connor.newsplatform.pojo.dto.ArticleDTO;
import com.connor.newsplatform.pojo.dto.AuditDTO;
import com.connor.newsplatform.pojo.dto.CommentDTO;
import com.connor.newsplatform.pojo.entity.AdminUser;
import com.connor.newsplatform.pojo.entity.AppUser;
import com.connor.newsplatform.pojo.entity.NewsArticle;
import com.connor.newsplatform.pojo.entity.NewsArticleComment;
import com.connor.newsplatform.pojo.entity.NewsArticleContent;
import com.connor.newsplatform.pojo.entity.NewsArticleFavorite;
import com.connor.newsplatform.pojo.entity.NewsArticleVote;
import com.connor.newsplatform.pojo.entity.NewsAuditRecord;
import com.connor.newsplatform.pojo.entity.NewsCategory;
import com.connor.newsplatform.pojo.entity.NewsReadRecord;
import com.connor.newsplatform.pojo.vo.ArticleVO;
import com.connor.newsplatform.pojo.vo.CommentVO;
import com.connor.newsplatform.server.mapper.AdminUserMapper;
import com.connor.newsplatform.server.mapper.AppUserMapper;
import com.connor.newsplatform.server.mapper.NewsArticleContentMapper;
import com.connor.newsplatform.server.mapper.NewsArticleCommentMapper;
import com.connor.newsplatform.server.mapper.NewsArticleFavoriteMapper;
import com.connor.newsplatform.server.mapper.NewsArticleMapper;
import com.connor.newsplatform.server.mapper.NewsArticleVoteMapper;
import com.connor.newsplatform.server.mapper.NewsAuditRecordMapper;
import com.connor.newsplatform.server.mapper.NewsCategoryMapper;
import com.connor.newsplatform.server.mapper.NewsReadRecordMapper;
import com.connor.newsplatform.server.service.ArticleService;
import com.connor.newsplatform.server.websocket.AuditWebSocketHandler;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final NewsArticleMapper articleMapper;
    private final NewsArticleContentMapper contentMapper;
    private final NewsCategoryMapper categoryMapper;
    private final NewsAuditRecordMapper auditRecordMapper;
    private final NewsReadRecordMapper readRecordMapper;
    private final NewsArticleVoteMapper voteMapper;
    private final NewsArticleFavoriteMapper favoriteMapper;
    private final NewsArticleCommentMapper commentMapper;
    private final AdminUserMapper adminUserMapper;
    private final AppUserMapper appUserMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditWebSocketHandler webSocketHandler;

    public ArticleServiceImpl(NewsArticleMapper articleMapper,
                              NewsArticleContentMapper contentMapper,
                              NewsCategoryMapper categoryMapper,
                              NewsAuditRecordMapper auditRecordMapper,
                              NewsReadRecordMapper readRecordMapper,
                              NewsArticleVoteMapper voteMapper,
                              NewsArticleFavoriteMapper favoriteMapper,
                              NewsArticleCommentMapper commentMapper,
                              AdminUserMapper adminUserMapper,
                              AppUserMapper appUserMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              AuditWebSocketHandler webSocketHandler) {
        this.articleMapper = articleMapper;
        this.contentMapper = contentMapper;
        this.categoryMapper = categoryMapper;
        this.auditRecordMapper = auditRecordMapper;
        this.readRecordMapper = readRecordMapper;
        this.voteMapper = voteMapper;
        this.favoriteMapper = favoriteMapper;
        this.commentMapper = commentMapper;
        this.adminUserMapper = adminUserMapper;
        this.appUserMapper = appUserMapper;
        this.redisTemplate = redisTemplate;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public PageResult<ArticleVO> adminPage(int page, int pageSize, String title, Long categoryId, Integer status,
                                           LocalDateTime beginTime, LocalDateTime endTime) {
        Page<NewsArticle> result = articleMapper.selectPage(Page.of(page, pageSize), new LambdaQueryWrapper<NewsArticle>()
                .like(StringUtils.hasText(title), NewsArticle::getTitle, title)
                .eq(categoryId != null, NewsArticle::getCategoryId, categoryId)
                .eq(status != null, NewsArticle::getStatus, status)
                .ge(beginTime != null, NewsArticle::getCreateTime, beginTime)
                .le(endTime != null, NewsArticle::getCreateTime, endTime)
                .orderByDesc(NewsArticle::getUpdateTime));
        return new PageResult<>(result.getTotal(), enrich(result.getRecords(), false));
    }

    @Override
    public ArticleVO adminDetail(Long id) {
        return detail(id, false);
    }

    @Override
    @Transactional
    public Long createDraft(ArticleDTO dto) {
        NewsArticle article = BeanCopy.to(dto, NewsArticle.class);
        article.setAuthorId(BaseContext.getCurrentId());
        article.setAuthorType(JwtClaimsConstant.ADMIN);
        article.setStatus(ArticleStatus.DRAFT);
        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setDislikeCount(0L);
        article.setFavoriteCount(0L);
        article.setCommentCount(0L);
        article.setHotScore(BigDecimal.ZERO);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        article.setCreateUser(BaseContext.getCurrentId());
        article.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.insert(article);

        NewsArticleContent content = new NewsArticleContent();
        content.setArticleId(article.getId());
        content.setContent(dto.getContent());
        contentMapper.insert(content);
        return article.getId();
    }

    @Override
    @Transactional
    public void submitUserArticle(ArticleDTO dto) {
        NewsArticle article = BeanCopy.to(dto, NewsArticle.class);
        article.setAuthorId(BaseContext.getCurrentId());
        article.setAuthorType(JwtClaimsConstant.USER);
        article.setStatus(ArticleStatus.PENDING);
        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setDislikeCount(0L);
        article.setFavoriteCount(0L);
        article.setCommentCount(0L);
        article.setHotScore(BigDecimal.ZERO);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        article.setCreateUser(BaseContext.getCurrentId());
        article.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.insert(article);

        NewsArticleContent content = new NewsArticleContent();
        content.setArticleId(article.getId());
        content.setContent(dto.getContent());
        contentMapper.insert(content);

        notifyAdmins("USER_ARTICLE_SUBMIT", "收到新的用户投稿",
                "用户投稿《%s》已进入待审核列表".formatted(article.getTitle()));
    }

    @Override
    @Transactional
    public void updateArticle(ArticleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("稿件ID不能为空");
        }
        NewsArticle article = articleMapper.selectById(dto.getId());
        if (article == null) {
            throw new BusinessException("稿件不存在");
        }
        NewsArticle update = BeanCopy.to(dto, NewsArticle.class);
        update.setId(dto.getId());
        update.setUpdateTime(LocalDateTime.now());
        update.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.updateById(update);
        NewsArticleContent content = contentMapper.selectOne(new LambdaQueryWrapper<NewsArticleContent>()
                .eq(NewsArticleContent::getArticleId, dto.getId()));
        if (content == null) {
            content = new NewsArticleContent();
            content.setArticleId(dto.getId());
            content.setContent(dto.getContent());
            contentMapper.insert(content);
        } else {
            content.setContent(dto.getContent());
            contentMapper.updateById(content);
        }
        clearArticleCache(dto.getId());
        notifyArticleAuthor(article, "ARTICLE_UPDATED", "稿件被管理员修改",
                "你的稿件《%s》已由管理员修改".formatted(article.getTitle()));
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        NewsArticle article = requireArticle(id);
        contentMapper.delete(new LambdaQueryWrapper<NewsArticleContent>()
                .eq(NewsArticleContent::getArticleId, id));
        voteMapper.delete(new LambdaQueryWrapper<NewsArticleVote>()
                .eq(NewsArticleVote::getArticleId, id));
        favoriteMapper.delete(new LambdaQueryWrapper<NewsArticleFavorite>()
                .eq(NewsArticleFavorite::getArticleId, id));
        commentMapper.delete(new LambdaQueryWrapper<NewsArticleComment>()
                .eq(NewsArticleComment::getArticleId, id));
        auditRecordMapper.delete(new LambdaQueryWrapper<NewsAuditRecord>()
                .eq(NewsAuditRecord::getArticleId, id));
        articleMapper.deleteById(id);
        clearArticleCache(id);
        notifyArticleAuthor(article, "ARTICLE_DELETED", "稿件已被删除",
                "你的稿件《%s》已由管理员删除".formatted(article.getTitle()));
    }

    @Override
    public void submit(Long id) {
        updateStatus(id, ArticleStatus.PENDING, null);
        NewsArticle article = requireArticle(id);
        notifyAdmins("ARTICLE_PENDING", "稿件提交审核",
                "稿件《%s》已提交审核".formatted(article.getTitle()));
    }

    @Override
    @Transactional
    public void approve(Long id, AuditDTO dto) {
        updateStatus(id, ArticleStatus.APPROVED, null);
        insertAuditRecord(id, ArticleStatus.APPROVED, dto == null ? null : dto.getAuditComment());
        pushAuditMessage(id, "已审核通过", "ARTICLE_APPROVED", "稿件审核通过");
    }

    @Override
    @Transactional
    public void reject(Long id, AuditDTO dto) {
        String comment = dto == null ? null : dto.getAuditComment();
        updateStatus(id, ArticleStatus.REJECTED, comment);
        insertAuditRecord(id, ArticleStatus.REJECTED, comment);
        pushAuditMessage(id, "已审核驳回", "ARTICLE_REJECTED", "稿件审核驳回");
    }

    @Override
    public void publish(Long id) {
        NewsArticle article = requireArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.APPROVED)
                && !Objects.equals(article.getStatus(), ArticleStatus.OFFLINE)) {
            throw new BusinessException("只有审核通过或已下架稿件可以发布");
        }
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setPublishTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        article.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.updateById(article);
        clearArticleCache(id);
        notifyArticleAuthor(article, "ARTICLE_PUBLISHED", "稿件已发布",
                "你的稿件《%s》已正式发布".formatted(article.getTitle()));
    }

    @Override
    public void offline(Long id) {
        NewsArticle article = requireArticle(id);
        updateStatus(id, ArticleStatus.OFFLINE, null);
        notifyArticleAuthor(article, "ARTICLE_OFFLINE", "稿件已下架",
                "你的稿件《%s》已由管理员下架".formatted(article.getTitle()));
    }

    @Override
    public PageResult<ArticleVO> userPage(int page, int pageSize, Long categoryId, String keyword, String sort) {
        boolean popularSort = "popular".equalsIgnoreCase(sort) || "hot".equalsIgnoreCase(sort);
        Page<NewsArticle> result = articleMapper.selectPage(Page.of(page, pageSize), new LambdaQueryWrapper<NewsArticle>()
                .eq(NewsArticle::getStatus, ArticleStatus.PUBLISHED)
                .eq(categoryId != null, NewsArticle::getCategoryId, categoryId)
                .like(StringUtils.hasText(keyword), NewsArticle::getTitle, keyword)
                .orderByDesc(popularSort, NewsArticle::getLikeCount)
                .orderByDesc(popularSort, NewsArticle::getCommentCount)
                .orderByDesc(popularSort, NewsArticle::getFavoriteCount)
                .orderByDesc(!popularSort, NewsArticle::getHotScore)
                .orderByDesc(NewsArticle::getPublishTime));
        return new PageResult<>(result.getTotal(), enrich(result.getRecords(), false));
    }

    @Override
    public ArticleVO userDetail(Long id) {
        if (BaseContext.getCurrentId() != null) {
            return detail(id, true);
        }
        String key = CacheKeys.ARTICLE_DETAIL_PREFIX + id;
        Object cached = getCachedValue(key);
        if (cached instanceof ArticleVO articleVO) {
            return articleVO;
        }
        ArticleVO vo = detail(id, true);
        redisTemplate.opsForValue().set(key, vo, Duration.ofMinutes(30));
        return vo;
    }

    @Override
    public PageResult<ArticleVO> userMinePage(int page, int pageSize, Integer status) {
        Page<NewsArticle> result = articleMapper.selectPage(Page.of(page, pageSize), new LambdaQueryWrapper<NewsArticle>()
                .eq(NewsArticle::getAuthorType, JwtClaimsConstant.USER)
                .eq(NewsArticle::getAuthorId, BaseContext.getCurrentId())
                .eq(status != null, NewsArticle::getStatus, status)
                .orderByDesc(NewsArticle::getUpdateTime));
        return new PageResult<>(result.getTotal(), enrich(result.getRecords(), false));
    }

    @Override
    public PageResult<ArticleVO> userFavoritePage(int page, int pageSize) {
        Page<NewsArticleFavorite> favoritePage = favoriteMapper.selectPage(Page.of(page, pageSize), new LambdaQueryWrapper<NewsArticleFavorite>()
                .eq(NewsArticleFavorite::getUserId, BaseContext.getCurrentId())
                .orderByDesc(NewsArticleFavorite::getCreateTime));
        List<Long> articleIds = favoritePage.getRecords().stream().map(NewsArticleFavorite::getArticleId).toList();
        if (articleIds.isEmpty()) {
            return new PageResult<>(favoritePage.getTotal(), List.of());
        }
        Map<Long, NewsArticle> articleMap = articleMapper.selectList(new LambdaQueryWrapper<NewsArticle>()
                        .in(NewsArticle::getId, articleIds)
                        .eq(NewsArticle::getStatus, ArticleStatus.PUBLISHED))
                .stream()
                .collect(Collectors.toMap(NewsArticle::getId, Function.identity()));
        List<NewsArticle> ordered = articleIds.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .toList();
        return new PageResult<>(favoritePage.getTotal(), enrich(ordered, false));
    }

    @Override
    @Transactional
    public ArticleVO vote(Long id, Integer voteType) {
        if (!Objects.equals(voteType, 1) && !Objects.equals(voteType, -1)) {
            throw new BusinessException("投票类型不正确");
        }
        NewsArticle article = requireArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED)) {
            throw new BusinessException("资讯未发布");
        }
        Long userId = BaseContext.getCurrentId();
        NewsArticleVote existing = voteMapper.selectOne(new LambdaQueryWrapper<NewsArticleVote>()
                .eq(NewsArticleVote::getArticleId, id)
                .eq(NewsArticleVote::getUserId, userId));
        if (existing == null) {
            NewsArticleVote vote = new NewsArticleVote();
            vote.setArticleId(id);
            vote.setUserId(userId);
            vote.setVoteType(voteType);
            vote.setCreateTime(LocalDateTime.now());
            vote.setUpdateTime(LocalDateTime.now());
            voteMapper.insert(vote);
            changeVoteCount(id, voteType, 1);
        } else if (!Objects.equals(existing.getVoteType(), voteType)) {
            int oldVote = existing.getVoteType() == null ? 0 : existing.getVoteType();
            existing.setVoteType(voteType);
            existing.setUpdateTime(LocalDateTime.now());
            voteMapper.updateById(existing);
            changeVoteCount(id, oldVote, -1);
            changeVoteCount(id, voteType, 1);
        }
        clearArticleCache(id);
        return detail(id, true);
    }

    @Override
    @Transactional
    public ArticleVO toggleFavorite(Long id) {
        NewsArticle article = requirePublishedArticle(id);
        Long userId = BaseContext.getCurrentId();
        NewsArticleFavorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<NewsArticleFavorite>()
                .eq(NewsArticleFavorite::getArticleId, id)
                .eq(NewsArticleFavorite::getUserId, userId));
        if (existing == null) {
            NewsArticleFavorite favorite = new NewsArticleFavorite();
            favorite.setArticleId(id);
            favorite.setUserId(userId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            changeArticleCounter(id, "favorite_count", 1);
        } else {
            favoriteMapper.deleteById(existing.getId());
            changeArticleCounter(id, "favorite_count", -1);
        }
        recalculateSingleHotScore(id);
        clearArticleCache(id);
        return detail(article.getId(), true);
    }

    @Override
    public PageResult<CommentVO> commentPage(Long id, int page, int pageSize) {
        requirePublishedArticle(id);
        Page<NewsArticleComment> result = commentMapper.selectPage(Page.of(page, pageSize), new LambdaQueryWrapper<NewsArticleComment>()
                .eq(NewsArticleComment::getArticleId, id)
                .eq(NewsArticleComment::getStatus, 1)
                .orderByDesc(NewsArticleComment::getCreateTime));
        return new PageResult<>(result.getTotal(), enrichComments(result.getRecords()));
    }

    @Override
    @Transactional
    public CommentVO addComment(Long id, CommentDTO dto) {
        requirePublishedArticle(id);
        NewsArticleComment comment = new NewsArticleComment();
        comment.setArticleId(id);
        comment.setUserId(BaseContext.getCurrentId());
        comment.setContent(dto.getContent().trim());
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        changeArticleCounter(id, "comment_count", 1);
        recalculateSingleHotScore(id);
        clearArticleCache(id);
        return enrichComments(List.of(comment)).getFirst();
    }

    @Override
    @Transactional
    public void recordRead(Long id) {
        requirePublishedArticle(id);
        NewsReadRecord record = new NewsReadRecord();
        record.setArticleId(id);
        record.setUserId(BaseContext.getCurrentId() == null ? 0L : BaseContext.getCurrentId());
        record.setCreateTime(LocalDateTime.now());
        readRecordMapper.insert(record);
        articleMapper.update(null, new LambdaUpdateWrapper<NewsArticle>()
                .eq(NewsArticle::getId, id)
                .setSql("view_count = view_count + 1")
                .setSql("hot_score = view_count + 1 + like_count * 4 + comment_count * 3 + favorite_count * 3 - dislike_count"));
        clearArticleCache(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ArticleVO> hotList(String sort) {
        String normalizedSort = normalizeHotSort(sort);
        Object cached = getCachedValue(hotCacheKey(normalizedSort));
        if (cached instanceof List<?> list) {
            return (List<ArticleVO>) list;
        }
        LambdaQueryWrapper<NewsArticle> wrapper = new LambdaQueryWrapper<NewsArticle>()
                .eq(NewsArticle::getStatus, ArticleStatus.PUBLISHED);
        if ("view".equals(normalizedSort)) {
            wrapper.orderByDesc(NewsArticle::getViewCount);
        } else if ("like".equals(normalizedSort)) {
            wrapper.orderByDesc(NewsArticle::getLikeCount);
        } else if ("popular".equals(normalizedSort)) {
            wrapper.orderByDesc(NewsArticle::getLikeCount)
                    .orderByDesc(NewsArticle::getCommentCount)
                    .orderByDesc(NewsArticle::getFavoriteCount);
        } else {
            wrapper.orderByDesc(NewsArticle::getHotScore);
        }
        wrapper.orderByDesc(NewsArticle::getPublishTime).last("LIMIT 10");
        List<ArticleVO> articles = new ArrayList<>(enrich(articleMapper.selectList(wrapper), false));
        redisTemplate.opsForValue().set(hotCacheKey(normalizedSort), articles, Duration.ofMinutes(10));
        return articles;
    }

    @Override
    public void recalculateHotScore() {
        List<NewsArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<NewsArticle>()
                .eq(NewsArticle::getStatus, ArticleStatus.PUBLISHED));
        LocalDateTime now = LocalDateTime.now();
        for (NewsArticle article : articles) {
            long hours = article.getPublishTime() == null ? 720 : Math.max(1, ChronoUnit.HOURS.between(article.getPublishTime(), now));
            BigDecimal recency = BigDecimal.valueOf(Math.max(0, 240 - hours)).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
            BigDecimal score = BigDecimal.valueOf(article.getViewCount() == null ? 0 : article.getViewCount())
                    .add(BigDecimal.valueOf(article.getLikeCount() == null ? 0 : article.getLikeCount()).multiply(BigDecimal.valueOf(4)))
                    .add(BigDecimal.valueOf(article.getCommentCount() == null ? 0 : article.getCommentCount()).multiply(BigDecimal.valueOf(3)))
                    .add(BigDecimal.valueOf(article.getFavoriteCount() == null ? 0 : article.getFavoriteCount()).multiply(BigDecimal.valueOf(3)))
                    .subtract(BigDecimal.valueOf(article.getDislikeCount() == null ? 0 : article.getDislikeCount()))
                    .add(recency.multiply(BigDecimal.valueOf(8)));
            NewsArticle update = new NewsArticle();
            update.setId(article.getId());
            update.setHotScore(score);
            articleMapper.updateById(update);
        }
        clearHotCaches();
    }

    private ArticleVO detail(Long id, boolean publishedOnly) {
        NewsArticle article = requireArticle(id);
        if (publishedOnly && !Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED)) {
            throw new BusinessException("资讯不存在或未发布");
        }
        ArticleVO vo = enrich(List.of(article), true).getFirst();
        return vo;
    }

    private List<ArticleVO> enrich(List<NewsArticle> articles, boolean withContent) {
        if (articles.isEmpty()) {
            return List.of();
        }
        List<Long> categoryIds = articles.stream().map(NewsArticle::getCategoryId).distinct().toList();
        Map<Long, NewsCategory> categoryMap = categoryMapper.selectBatchIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(NewsCategory::getId, Function.identity()));
        Map<Long, NewsArticleContent> contentMap = Map.of();
        if (withContent) {
            List<Long> ids = articles.stream().map(NewsArticle::getId).toList();
            contentMap = contentMapper.selectList(new LambdaQueryWrapper<NewsArticleContent>()
                            .in(NewsArticleContent::getArticleId, ids))
                    .stream()
                    .collect(Collectors.toMap(NewsArticleContent::getArticleId, Function.identity(), (a, b) -> a));
        }
        Map<Long, NewsArticleContent> finalContentMap = contentMap;
        return articles.stream().map(article -> {
            ArticleVO vo = BeanCopy.to(article, ArticleVO.class);
            NewsCategory category = categoryMap.get(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
            vo.setAuthorName(resolveAuthorName(article));
            if (BaseContext.getCurrentId() != null && JwtClaimsConstant.USER.equals(BaseContext.getCurrentType())) {
                NewsArticleVote vote = voteMapper.selectOne(new LambdaQueryWrapper<NewsArticleVote>()
                        .eq(NewsArticleVote::getArticleId, article.getId())
                        .eq(NewsArticleVote::getUserId, BaseContext.getCurrentId()));
                if (vote != null) {
                    vo.setUserVote(vote.getVoteType());
                }
                vo.setFavorited(favoriteMapper.exists(new LambdaQueryWrapper<NewsArticleFavorite>()
                        .eq(NewsArticleFavorite::getArticleId, article.getId())
                        .eq(NewsArticleFavorite::getUserId, BaseContext.getCurrentId())));
            }
            NewsArticleContent content = finalContentMap.get(article.getId());
            if (content != null) {
                vo.setContent(content.getContent());
            }
            return vo;
        }).toList();
    }

    private NewsArticle requireArticle(Long id) {
        NewsArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("稿件不存在");
        }
        return article;
    }

    private NewsArticle requirePublishedArticle(Long id) {
        NewsArticle article = requireArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED)) {
            throw new BusinessException("资讯未发布");
        }
        return article;
    }

    private void updateStatus(Long id, Integer status, String rejectReason) {
        NewsArticle article = requireArticle(id);
        article.setStatus(status);
        article.setRejectReason(rejectReason);
        article.setUpdateTime(LocalDateTime.now());
        article.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.updateById(article);
        clearArticleCache(id);
    }

    private void changeVoteCount(Long id, int voteType, int delta) {
        if (voteType == 1) {
            changeArticleCounter(id, "like_count", delta);
        } else if (voteType == -1) {
            changeArticleCounter(id, "dislike_count", delta);
        }
        recalculateSingleHotScore(id);
    }

    private void changeArticleCounter(Long id, String column, int delta) {
        articleMapper.update(null, new LambdaUpdateWrapper<NewsArticle>()
                .eq(NewsArticle::getId, id)
                .setSql(column + " = GREATEST(" + column + " + (" + delta + "), 0)"));
    }

    private void recalculateSingleHotScore(Long id) {
        NewsArticle article = requireArticle(id);
        LocalDateTime now = LocalDateTime.now();
        long hours = article.getPublishTime() == null ? 720 : Math.max(1, ChronoUnit.HOURS.between(article.getPublishTime(), now));
        BigDecimal recency = BigDecimal.valueOf(Math.max(0, 240 - hours)).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        BigDecimal score = BigDecimal.valueOf(article.getViewCount() == null ? 0 : article.getViewCount())
                .add(BigDecimal.valueOf(article.getLikeCount() == null ? 0 : article.getLikeCount()).multiply(BigDecimal.valueOf(4)))
                .add(BigDecimal.valueOf(article.getCommentCount() == null ? 0 : article.getCommentCount()).multiply(BigDecimal.valueOf(3)))
                .add(BigDecimal.valueOf(article.getFavoriteCount() == null ? 0 : article.getFavoriteCount()).multiply(BigDecimal.valueOf(3)))
                .subtract(BigDecimal.valueOf(article.getDislikeCount() == null ? 0 : article.getDislikeCount()))
                .add(recency.multiply(BigDecimal.valueOf(8)));
        NewsArticle update = new NewsArticle();
        update.setId(id);
        update.setHotScore(score);
        articleMapper.updateById(update);
    }

    private void insertAuditRecord(Long id, Integer status, String comment) {
        NewsAuditRecord record = new NewsAuditRecord();
        record.setArticleId(id);
        record.setAuditorId(BaseContext.getCurrentId());
        record.setAuditStatus(status);
        record.setAuditComment(comment);
        record.setCreateTime(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }

    private void pushAuditMessage(Long id, String statusText, String type, String title) {
        NewsArticle article = requireArticle(id);
        String content = "稿件《%s》%s".formatted(article.getTitle(), statusText);
        notifyAdmins("ARTICLE_AUDIT", "稿件审核状态更新", content);
        notifyArticleAuthor(article, type, title, content);
    }

    private void notifyAdmins(String type, String title, String content) {
        String message = buildMessage(type, title, content);
        webSocketHandler.broadcastAdmins(message);
    }

    private void notifyArticleAuthor(NewsArticle article, String type, String title, String content) {
        if (article != null && JwtClaimsConstant.USER.equals(article.getAuthorType())) {
            webSocketHandler.sendToUser(article.getAuthorId(), buildMessage(type, title, content));
        }
    }

    private String buildMessage(String type, String title, String content) {
        return "{\"type\":\"%s\",\"title\":\"%s\",\"content\":\"%s\",\"time\":\"%s\"}"
                .formatted(escapeJson(type), escapeJson(title), escapeJson(content), DateTimeUtil.format(LocalDateTime.now()));
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String resolveAuthorName(NewsArticle article) {
        if (JwtClaimsConstant.USER.equals(article.getAuthorType())) {
            AppUser user = appUserMapper.selectById(article.getAuthorId());
            if (user != null) {
                return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
            }
            return "用户投稿";
        }
        AdminUser admin = adminUserMapper.selectById(article.getAuthorId());
        if (admin != null) {
            return StringUtils.hasText(admin.getName()) ? admin.getName() : admin.getUsername();
        }
        return "平台编辑";
    }

    private List<CommentVO> enrichComments(List<NewsArticleComment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = comments.stream().map(NewsArticleComment::getUserId).distinct().toList();
        Map<Long, AppUser> userMap = appUserMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(AppUser::getId, Function.identity()));
        return comments.stream().map(comment -> {
            CommentVO vo = BeanCopy.to(comment, CommentVO.class);
            AppUser user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).toList();
    }

    private void clearArticleCache(Long id) {
        clearHotCaches();
        redisTemplate.delete(CacheKeys.DASHBOARD_TODAY);
        if (id != null) {
            redisTemplate.delete(CacheKeys.ARTICLE_DETAIL_PREFIX + id);
        }
    }

    private String normalizeHotSort(String sort) {
        if ("view".equalsIgnoreCase(sort) || "like".equalsIgnoreCase(sort) || "popular".equalsIgnoreCase(sort)) {
            return sort.toLowerCase();
        }
        return "latest";
    }

    private String hotCacheKey(String sort) {
        return CacheKeys.ARTICLE_HOT + ":" + sort;
    }

    private void clearHotCaches() {
        redisTemplate.delete(List.of(
                CacheKeys.ARTICLE_HOT,
                hotCacheKey("hot"),
                hotCacheKey("latest"),
                hotCacheKey("view"),
                hotCacheKey("like"),
                hotCacheKey("popular")
        ));
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
