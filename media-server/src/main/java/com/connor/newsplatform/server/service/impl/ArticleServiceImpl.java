package com.connor.newsplatform.server.service.impl;

import com.connor.newsplatform.common.constant.ArticleStatus;
import com.connor.newsplatform.common.constant.JwtClaimsConstant;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.dto.ArticleDTO;
import com.connor.newsplatform.pojo.dto.AuditDTO;
import com.connor.newsplatform.pojo.dto.CommentDTO;
import com.connor.newsplatform.pojo.entity.*;
import com.connor.newsplatform.pojo.vo.ArticleVO;
import com.connor.newsplatform.pojo.vo.CommentVO;
import com.connor.newsplatform.server.mapper.*;
import com.connor.newsplatform.server.service.ArticleService;
import com.connor.newsplatform.server.websocket.AuditWebSocketHandler;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章服务（纯MyBatis版，参照苍穹外卖风格）
 */
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
        this.webSocketHandler = webSocketHandler;
    }

    // ==================== 管理端 ====================

    /**
     * 管理端分页查询
     */
    @Override
    public PageResult<ArticleVO> adminPage(int page, int pageSize, String title, Long categoryId,
                                           Integer status, LocalDateTime beginTime, LocalDateTime endTime) {
        PageHelper.startPage(page, pageSize);
        Page<NewsArticle> p = articleMapper.adminPageQuery(title, categoryId, status, beginTime, endTime);
        return new PageResult<>(p.getTotal(), toVOList(p.getResult(), false));
    }

    /**
     * 管理端查询详情
     */
    @Override
    public ArticleVO adminDetail(Long id) {
        return toVO(getArticle(id), true);
    }

    /**
     * 创建草稿
     */
    @Override
    @Transactional
    public Long createDraft(ArticleDTO dto) {
        NewsArticle article = new NewsArticle();
        BeanUtils.copyProperties(dto, article);
        article.setAuthorId(BaseContext.getCurrentId());
        article.setAuthorType(JwtClaimsConstant.ADMIN);
        article.setStatus(ArticleStatus.DRAFT);
        setDefaultCounts(article);
        setCreateMeta(article);
        articleMapper.insert(article);

        // 保存正文
        insertContent(article.getId(), dto.getContent());
        return article.getId();
    }

    /**
     * 用户投稿
     */
    @Override
    @Transactional
    public void submitUserArticle(ArticleDTO dto) {
        NewsArticle article = new NewsArticle();
        BeanUtils.copyProperties(dto, article);
        article.setAuthorId(BaseContext.getCurrentId());
        article.setAuthorType(JwtClaimsConstant.USER);
        article.setStatus(ArticleStatus.PENDING);
        setDefaultCounts(article);
        setCreateMeta(article);
        articleMapper.insert(article);

        insertContent(article.getId(), dto.getContent());

        // WebSocket通知管理员
        notifyAdmins("USER_ARTICLE_SUBMIT", "收到新的用户投稿",
                "用户投稿《" + article.getTitle() + "》已进入待审核列表");
    }

    /**
     * 修改稿件
     */
    @Override
    @Transactional
    public void updateArticle(ArticleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("稿件ID不能为空");
        }
        NewsArticle article = getArticle(dto.getId());

        NewsArticle update = new NewsArticle();
        BeanUtils.copyProperties(dto, update);
        update.setUpdateTime(LocalDateTime.now());
        update.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.update(update);

        // 更新正文
        NewsArticleContent content = contentMapper.getByArticleId(dto.getId());
        if (content == null) {
            insertContent(dto.getId(), dto.getContent());
        } else {
            content.setContent(dto.getContent());
            // 没有update方法，用insert+delete方式（或者加一个update方法）
            contentMapper.deleteByArticleId(dto.getId());
            insertContent(dto.getId(), dto.getContent());
        }

        // 通知作者
        if (JwtClaimsConstant.USER.equals(article.getAuthorType())) {
            notifyUser(article.getAuthorId(), "ARTICLE_UPDATED", "稿件被管理员修改",
                    "你的稿件《" + article.getTitle() + "》已由管理员修改");
        }
    }

    /**
     * 删除稿件
     */
    @Override
    @Transactional
    public void deleteArticle(Long id) {
        NewsArticle article = getArticle(id);
        // 删除关联数据
        contentMapper.deleteByArticleId(id);
        voteMapper.deleteByArticleId(id);
        favoriteMapper.deleteByArticleId(id);
        commentMapper.deleteByArticleId(id);
        auditRecordMapper.deleteByArticleId(id);
        articleMapper.deleteById(id);

        // 通知作者
        if (JwtClaimsConstant.USER.equals(article.getAuthorType())) {
            notifyUser(article.getAuthorId(), "ARTICLE_DELETED", "稿件已被删除",
                    "你的稿件《" + article.getTitle() + "》已由管理员删除");
        }
    }

    /**
     * 提交审核
     */
    @Override
    public void submit(Long id) {
        updateStatus(id, ArticleStatus.PENDING, null);
        NewsArticle article = getArticle(id);
        notifyAdmins("ARTICLE_PENDING", "稿件提交审核",
                "稿件《" + article.getTitle() + "》已提交审核");
    }

    /**
     * 审核通过
     */
    @Override
    @Transactional
    public void approve(Long id, AuditDTO dto) {
        updateStatus(id, ArticleStatus.APPROVED, null);
        insertAuditRecord(id, ArticleStatus.APPROVED, dto == null ? null : dto.getAuditComment());
        pushAuditMessage(id, "已审核通过", "ARTICLE_APPROVED", "稿件审核通过");
    }

    /**
     * 审核驳回
     */
    @Override
    @Transactional
    public void reject(Long id, AuditDTO dto) {
        String comment = dto == null ? null : dto.getAuditComment();
        updateStatus(id, ArticleStatus.REJECTED, comment);
        insertAuditRecord(id, ArticleStatus.REJECTED, comment);
        pushAuditMessage(id, "已审核驳回", "ARTICLE_REJECTED", "稿件审核驳回");
    }

    /**
     * 发布稿件
     */
    @Override
    public void publish(Long id) {
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.APPROVED)
                && !Objects.equals(article.getStatus(), ArticleStatus.OFFLINE)) {
            throw new BusinessException("只有审核通过或已下架稿件可以发布");
        }
        NewsArticle update = new NewsArticle();
        update.setId(id);
        update.setStatus(ArticleStatus.PUBLISHED);
        update.setPublishTime(LocalDateTime.now());
        update.setUpdateTime(LocalDateTime.now());
        update.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.update(update);

        if (JwtClaimsConstant.USER.equals(article.getAuthorType())) {
            notifyUser(article.getAuthorId(), "ARTICLE_PUBLISHED", "稿件已发布",
                    "你的稿件《" + article.getTitle() + "》已正式发布");
        }
    }

    /**
     * 下架稿件
     */
    @Override
    public void offline(Long id) {
        NewsArticle article = getArticle(id);
        updateStatus(id, ArticleStatus.OFFLINE, null);
        if (JwtClaimsConstant.USER.equals(article.getAuthorType())) {
            notifyUser(article.getAuthorId(), "ARTICLE_OFFLINE", "稿件已下架",
                    "你的稿件《" + article.getTitle() + "》已由管理员下架");
        }
    }

    // ==================== 用户端 ====================

    /**
     * 用户端分页查询（已发布）
     */
    @Override
    public PageResult<ArticleVO> userPage(int page, int pageSize, Long categoryId, String keyword, String sort) {
        PageHelper.startPage(page, pageSize);
        Page<NewsArticle> p = articleMapper.userPageQuery(categoryId, keyword, sort);
        return new PageResult<>(p.getTotal(), toVOList(p.getResult(), false));
    }

    /**
     * 用户端查询详情
     */
    @Override
    public ArticleVO userDetail(Long id) {
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED)) {
            throw new BusinessException("资讯不存在或未发布");
        }
        return toVO(article, true);
    }

    /**
     * 用户我的稿件分页
     */
    @Override
    public PageResult<ArticleVO> userMinePage(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        Page<NewsArticle> p = articleMapper.userMinePageQuery(BaseContext.getCurrentId(), status);
        return new PageResult<>(p.getTotal(), toVOList(p.getResult(), false));
    }

    /**
     * 用户收藏列表
     */
    @Override
    public PageResult<ArticleVO> userFavoritePage(int page, int pageSize) {
        // 1. 分页查询收藏记录
        PageHelper.startPage(page, pageSize);
        Page<NewsArticleFavorite> favoritePage = favoriteMapper.pageByUserId(BaseContext.getCurrentId());

        // 2. 取出文章id
        List<Long> articleIds = favoritePage.getResult().stream()
                .map(NewsArticleFavorite::getArticleId)
                .collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return new PageResult<>(favoritePage.getTotal(), List.of());
        }

        // 3. 查询文章
        List<NewsArticle> articles = articleMapper.selectByIds(articleIds);
        // 只保留已发布的
        List<NewsArticle> published = articles.stream()
                .filter(a -> Objects.equals(a.getStatus(), ArticleStatus.PUBLISHED))
                .collect(Collectors.toList());

        return new PageResult<>(favoritePage.getTotal(), toVOList(published, false));
    }

    /**
     * 点赞/点踩
     */
    @Override
    @Transactional
    public ArticleVO vote(Long id, Integer voteType) {
        if (!Objects.equals(voteType, 1) && !Objects.equals(voteType, -1)) {
            throw new BusinessException("投票类型不正确");
        }
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED)) {
            throw new BusinessException("资讯未发布");
        }

        Long userId = BaseContext.getCurrentId();
        NewsArticleVote existing = voteMapper.getByArticleAndUser(id, userId);

        if (existing == null) {
            // 新增投票
            NewsArticleVote vote = new NewsArticleVote();
            vote.setArticleId(id);
            vote.setUserId(userId);
            vote.setVoteType(voteType);
            vote.setCreateTime(LocalDateTime.now());
            vote.setUpdateTime(LocalDateTime.now());
            voteMapper.insert(vote);
            changeVoteCount(id, voteType, 1);
        } else if (!Objects.equals(existing.getVoteType(), voteType)) {
            // 修改投票
            int oldVote = existing.getVoteType() == null ? 0 : existing.getVoteType();
            existing.setVoteType(voteType);
            existing.setUpdateTime(LocalDateTime.now());
            voteMapper.update(existing);
            changeVoteCount(id, oldVote, -1);
            changeVoteCount(id, voteType, 1);
        }

        return toVO(getArticle(id), true);
    }

    /**
     * 收藏/取消收藏
     */
    @Override
    @Transactional
    public ArticleVO toggleFavorite(Long id) {
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED)) {
            throw new BusinessException("资讯未发布");
        }

        Long userId = BaseContext.getCurrentId();
        NewsArticleFavorite existing = favoriteMapper.getByUserAndArticle(userId, id);

        if (existing == null) {
            // 新增收藏
            NewsArticleFavorite favorite = new NewsArticleFavorite();
            favorite.setArticleId(id);
            favorite.setUserId(userId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            articleMapper.incrementFavoriteCount(id, 1);
        } else {
            // 取消收藏
            favoriteMapper.deleteById(existing.getId());
            articleMapper.incrementFavoriteCount(id, -1);
        }

        return toVO(getArticle(id), true);
    }

    /**
     * 评论分页
     */
    @Override
    public PageResult<CommentVO> commentPage(Long id, int page, int pageSize) {
        getArticle(id);
        PageHelper.startPage(page, pageSize);
        Page<NewsArticleComment> p = commentMapper.pageByArticleId(id);

        List<CommentVO> list = p.getResult().stream()
                .map(comment -> {
                    CommentVO vo = new CommentVO();
                    BeanUtils.copyProperties(comment, vo);
                    AppUser user = appUserMapper.getById(comment.getUserId());
                    if (user != null) {
                        vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                        vo.setAvatar(user.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(p.getTotal(), list);
    }

    /**
     * 发表评论
     */
    @Override
    @Transactional
    public CommentVO addComment(Long id, CommentDTO dto) {
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED)) {
            throw new BusinessException("资讯未发布");
        }

        NewsArticleComment comment = new NewsArticleComment();
        comment.setArticleId(id);
        comment.setUserId(BaseContext.getCurrentId());
        comment.setContent(dto.getContent().trim());
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        articleMapper.incrementCommentCount(id, 1);

        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        AppUser user = appUserMapper.getById(comment.getUserId());
        if (user != null) {
            vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }

    /**
     * 记录阅读行为
     */
    @Override
    public void recordRead(Long id) {
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED)) {
            throw new BusinessException("资讯未发布");
        }

        NewsReadRecord record = new NewsReadRecord();
        record.setArticleId(id);
        record.setUserId(BaseContext.getCurrentId() == null ? 0L : BaseContext.getCurrentId());
        record.setCreateTime(LocalDateTime.now());
        readRecordMapper.insert(record);

        articleMapper.incrementViewCount(id);
    }

    /**
     * 热点列表
     */
    @Override
    public List<ArticleVO> hotList(String sort) {
        String normalizedSort = normalizeSort(sort);
        List<NewsArticle> articles = articleMapper.selectHotList(normalizedSort);
        return toVOList(articles, false);
    }

    /**
     * 重新计算所有已发布稿件的热度分（定时任务调用）
     */
    @Override
    public void recalculateHotScore() {
        List<NewsArticle> articles = articleMapper.selectAllPublished();
        LocalDateTime now = LocalDateTime.now();
        for (NewsArticle article : articles) {
            long hours = article.getPublishTime() == null
                    ? 720 : Math.max(1, ChronoUnit.HOURS.between(article.getPublishTime(), now));
            // 时间衰减因子：发布越久分越低
            BigDecimal recency = BigDecimal.valueOf(Math.max(0, 240 - hours))
                    .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
            // 热度分 = 阅读 + 点赞*4 + 评论*3 + 收藏*3 - 点踩 + 时间衰减*8
            BigDecimal score = BigDecimal.valueOf(nvl(article.getViewCount()))
                    .add(BigDecimal.valueOf(nvl(article.getLikeCount())).multiply(BigDecimal.valueOf(4)))
                    .add(BigDecimal.valueOf(nvl(article.getCommentCount())).multiply(BigDecimal.valueOf(3)))
                    .add(BigDecimal.valueOf(nvl(article.getFavoriteCount())).multiply(BigDecimal.valueOf(3)))
                    .subtract(BigDecimal.valueOf(nvl(article.getDislikeCount())))
                    .add(recency.multiply(BigDecimal.valueOf(8)));
            articleMapper.updateHotScore(article.getId(), score);
        }
    }

    // ==================== 私有方法 ====================

    /** 查询稿件，不存在则抛异常 */
    private NewsArticle getArticle(Long id) {
        NewsArticle article = articleMapper.getById(id);
        if (article == null) {
            throw new BusinessException("稿件不存在");
        }
        return article;
    }

    /** Entity列表转VO列表 */
    private List<ArticleVO> toVOList(List<NewsArticle> articles, boolean withContent) {
        List<ArticleVO> list = new ArrayList<>();
        for (NewsArticle article : articles) {
            list.add(toVO(article, withContent));
        }
        return list;
    }

    /** Entity转VO（填充分类名、作者名、正文、用户投票/收藏状态） */
    private ArticleVO toVO(NewsArticle article, boolean withContent) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);

        // 填充分类名
        NewsCategory category = categoryMapper.getById(article.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 填充作者名
        vo.setAuthorName(resolveAuthorName(article));

        // 填充正文
        if (withContent) {
            NewsArticleContent content = contentMapper.getByArticleId(article.getId());
            if (content != null) {
                vo.setContent(content.getContent());
            }
        }

        // 填充当前用户的投票和收藏状态
        if (BaseContext.getCurrentId() != null && JwtClaimsConstant.USER.equals(BaseContext.getCurrentType())) {
            NewsArticleVote vote = voteMapper.getByArticleAndUser(article.getId(), BaseContext.getCurrentId());
            if (vote != null) {
                vo.setUserVote(vote.getVoteType());
            }
            NewsArticleFavorite favorite = favoriteMapper.getByUserAndArticle(BaseContext.getCurrentId(), article.getId());
            vo.setFavorited(favorite != null);
        }

        return vo;
    }

    /** 解析作者名 */
    private String resolveAuthorName(NewsArticle article) {
        if (JwtClaimsConstant.USER.equals(article.getAuthorType())) {
            AppUser user = appUserMapper.getById(article.getAuthorId());
            if (user != null) {
                return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
            }
            return "用户投稿";
        }
        AdminUser admin = adminUserMapper.getById(article.getAuthorId());
        if (admin != null) {
            return StringUtils.hasText(admin.getName()) ? admin.getName() : admin.getUsername();
        }
        return "平台编辑";
    }

    /** 新增正文 */
    private void insertContent(Long articleId, String content) {
        NewsArticleContent entity = new NewsArticleContent();
        entity.setArticleId(articleId);
        entity.setContent(content);
        contentMapper.insert(entity);
    }

    /** 更新稿件状态 */
    private void updateStatus(Long id, Integer status, String rejectReason) {
        NewsArticle update = new NewsArticle();
        update.setId(id);
        update.setStatus(status);
        update.setRejectReason(rejectReason);
        update.setUpdateTime(LocalDateTime.now());
        update.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.update(update);
    }

    /** 新增审核记录 */
    private void insertAuditRecord(Long articleId, Integer status, String comment) {
        NewsAuditRecord record = new NewsAuditRecord();
        record.setArticleId(articleId);
        record.setAuditorId(BaseContext.getCurrentId());
        record.setAuditStatus(status);
        record.setAuditComment(comment);
        record.setCreateTime(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }

    /** 修改点赞/点踩数 */
    private void changeVoteCount(Long id, int voteType, int delta) {
        if (voteType == 1) {
            articleMapper.incrementLikeCount(id, delta);
        } else if (voteType == -1) {
            articleMapper.incrementDislikeCount(id, delta);
        }
    }

    /** 设置默认计数字段 */
    private void setDefaultCounts(NewsArticle article) {
        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setDislikeCount(0L);
        article.setFavoriteCount(0L);
        article.setCommentCount(0L);
        article.setHotScore(BigDecimal.ZERO);
    }

    /** 设置创建时间、更新时间、创建人、修改人 */
    private void setCreateMeta(NewsArticle article) {
        LocalDateTime now = LocalDateTime.now();
        article.setCreateTime(now);
        article.setUpdateTime(now);
        article.setCreateUser(BaseContext.getCurrentId());
        article.setUpdateUser(BaseContext.getCurrentId());
    }

    /** 推送审核消息（通知管理员+作者） */
    private void pushAuditMessage(Long id, String statusText, String type, String title) {
        NewsArticle article = getArticle(id);
        String content = "稿件《" + article.getTitle() + "》" + statusText;
        notifyAdmins("ARTICLE_AUDIT", "稿件审核状态更新", content);
        if (JwtClaimsConstant.USER.equals(article.getAuthorType())) {
            notifyUser(article.getAuthorId(), type, title, content);
        }
    }

    /** 通知管理员 */
    private void notifyAdmins(String type, String title, String content) {
        String message = buildMessage(type, title, content);
        webSocketHandler.broadcastAdmins(message);
    }

    /** 通知用户 */
    private void notifyUser(Long userId, String type, String title, String content) {
        String message = buildMessage(type, title, content);
        webSocketHandler.sendToUser(userId, message);
    }

    /** 构建WebSocket消息 */
    private String buildMessage(String type, String title, String content) {
        return "{\"type\":\"" + type + "\",\"title\":\"" + title
                + "\",\"content\":\"" + content + "\",\"time\":\""
                + LocalDateTime.now() + "\"}";
    }

    /** 归一化排序参数 */
    private String normalizeSort(String sort) {
        if ("view".equalsIgnoreCase(sort) || "like".equalsIgnoreCase(sort) || "popular".equalsIgnoreCase(sort)) {
            return sort.toLowerCase();
        }
        return "latest";
    }

    /** null转0 */
    private long nvl(Long val) {
        return val == null ? 0 : val;
    }
}
