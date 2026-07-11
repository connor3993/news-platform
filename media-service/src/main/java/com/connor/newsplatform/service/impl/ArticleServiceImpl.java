package com.connor.newsplatform.service.impl;

import com.connor.newsplatform.common.constant.ArticleStatus;
import com.connor.newsplatform.common.constant.JwtClaimsConstant;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.domain.req.ArticleDTO;
import com.connor.newsplatform.domain.req.AuditDTO;
import com.connor.newsplatform.domain.po.*;
import com.connor.newsplatform.domain.vo.ArticleVO;
import com.connor.newsplatform.dao.*;
import com.connor.newsplatform.service.ArticleService;
import com.connor.newsplatform.service.websocket.AuditWebSocketHandler;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章服务（纯MyBatis版，参照苍穹外卖风格）
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final String ACTION_LIKE = "like";
    private static final String ACTION_DISLIKE = "dislike";
    private static final String ACTION_FAVORITE = "favorite";

    private final NewsArticleMapper articleMapper;
    private final NewsCategoryMapper categoryMapper;
    private final NewsReadRecordMapper readRecordMapper;
    private final NewsUserInteractionMapper interactionMapper;
    private final SysUserMapper sysUserMapper;
    private final com.connor.newsplatform.dao.PayOrderMapper payOrderMapper;
    private final AuditWebSocketHandler webSocketHandler;

    public ArticleServiceImpl(NewsArticleMapper articleMapper,
                              NewsCategoryMapper categoryMapper,
                              NewsReadRecordMapper readRecordMapper,
                              NewsUserInteractionMapper interactionMapper,
                              SysUserMapper sysUserMapper,
                              com.connor.newsplatform.dao.PayOrderMapper payOrderMapper,
                              AuditWebSocketHandler webSocketHandler) {
        this.articleMapper = articleMapper;
        this.categoryMapper = categoryMapper;
        this.readRecordMapper = readRecordMapper;
        this.interactionMapper = interactionMapper;
        this.sysUserMapper = sysUserMapper;
        this.payOrderMapper = payOrderMapper;
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
        article.setStatus(ArticleStatus.DRAFT.name());
        setDefaultCounts(article);
        setCreateMeta(article);
        articleMapper.insert(article);
        return article.getId();
    }

    /**
     * 用户投稿
     */
    @Override
    @Transactional
    public void submitUserArticle(ArticleDTO dto) {
        validatePaid(dto);
        NewsArticle article = new NewsArticle();
        BeanUtils.copyProperties(dto, article);
        article.setAuthorId(BaseContext.getCurrentId());
        article.setStatus(ArticleStatus.PENDING.name());
        setDefaultCounts(article);
        setCreateMeta(article);
        articleMapper.insert(article);

        // WebSocket通知管理员
        notifyAdmins("USER_ARTICLE_SUBMIT", "收到新的用户投稿",
                "用户投稿《" + article.getTitle() + "》已进入待审核列表");
    }

    /**
     * 校验付费字段：付费文章必须填价格>0、预览、付费内容
     */
    private void validatePaid(ArticleDTO dto) {
        boolean paid = dto.getIsPaid() != null && dto.getIsPaid() == 1;
        if (paid) {
            if (dto.getPrice() == null || dto.getPrice().signum() <= 0) {
                throw new BusinessException("付费文章必须设置大于 0 的价格");
            }
            if (!StringUtils.hasText(dto.getPaidPreview())) {
                throw new BusinessException("付费文章必须填写预览内容");
            }
            if (!StringUtils.hasText(dto.getPaidOnly())) {
                throw new BusinessException("付费文章必须填写付费内容");
            }
        }
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
        validatePaid(dto);
        NewsArticle article = getArticle(dto.getId());

        NewsArticle update = new NewsArticle();
        BeanUtils.copyProperties(dto, update);
        update.setUpdateTime(LocalDateTime.now());
        update.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.update(update);

        // 更新正文（正文已并入 news_article 表）
        if (dto.getContent() != null) {
            NewsArticle contentUpdate = new NewsArticle();
            contentUpdate.setId(dto.getId());
            contentUpdate.setContent(dto.getContent());
            contentUpdate.setUpdateTime(LocalDateTime.now());
            articleMapper.update(contentUpdate);
        }

        // 通知作者
        if (isUserAuthor(article)) {
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
        // 删除关联互动数据（正文为 news_article 的列，随文章一起删除）
        interactionMapper.deleteByArticleId(id);
        articleMapper.deleteById(id);

        // 通知作者
        if (isUserAuthor(article)) {
            notifyUser(article.getAuthorId(), "ARTICLE_DELETED", "稿件已被删除",
                    "你的稿件《" + article.getTitle() + "》已由管理员删除");
        }
    }

    /**
     * 提交审核
     */
    @Override
    public void submit(Long id) {
        updateStatus(id, ArticleStatus.PENDING.name(), null);
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
        updateStatus(id, ArticleStatus.APPROVED.name(), null);
        pushAuditMessage(id, "已审核通过", "ARTICLE_APPROVED", "稿件审核通过");
    }

    /**
     * 审核驳回
     */
    @Override
    @Transactional
    public void reject(Long id, AuditDTO dto) {
        String comment = dto == null ? null : dto.getAuditComment();
        updateStatus(id, ArticleStatus.REJECTED.name(), comment);
        pushAuditMessage(id, "已审核驳回", "ARTICLE_REJECTED", "稿件审核驳回");
    }

    /**
     * 发布稿件
     */
    @Override
    public void publish(Long id) {
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.APPROVED.name())
                && !Objects.equals(article.getStatus(), ArticleStatus.OFFLINE.name())) {
            throw new BusinessException("只有审核通过或已下架稿件可以发布");
        }
        NewsArticle update = new NewsArticle();
        update.setId(id);
        update.setStatus(ArticleStatus.PUBLISHED.name());
        update.setPublishTime(LocalDateTime.now());
        update.setUpdateTime(LocalDateTime.now());
        update.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.update(update);

        if (isUserAuthor(article)) {
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
        updateStatus(id, ArticleStatus.OFFLINE.name(), null);
        if (isUserAuthor(article)) {
            notifyUser(article.getAuthorId(), "ARTICLE_OFFLINE", "稿件已下架",
                    "你的稿件《" + article.getTitle() + "》已由管理员下架");
        }
    }

    // ==================== 用户端 ====================

    /**
     * 用户端分页查询（已发布）
     */
    @Override
    public PageResult<ArticleVO> userPage(int page, int pageSize, Long categoryId, String keyword) {
        PageHelper.startPage(page, pageSize);
        Page<NewsArticle> p = articleMapper.userPageQuery(categoryId, keyword);
        return new PageResult<>(p.getTotal(), toVOList(p.getResult(), false));
    }

    /**
     * 用户端查询详情
     */
    @Override
    public ArticleVO userDetail(Long id) {
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED.name())) {
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
        // 1. 分页查询收藏互动记录
        PageHelper.startPage(page, pageSize);
        Page<NewsUserInteraction> favoritePage = interactionMapper.pageByUserAndAction(BaseContext.getCurrentId(), ACTION_FAVORITE);

        // 2. 取出文章id
        List<Long> articleIds = favoritePage.getResult().stream()
                .map(NewsUserInteraction::getArticleId)
                .collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return new PageResult<>(favoritePage.getTotal(), List.of());
        }

        // 3. 查询文章
        List<NewsArticle> articles = articleMapper.selectByIds(articleIds);
        // 只保留已发布的
        List<NewsArticle> published = articles.stream()
                .filter(a -> Objects.equals(a.getStatus(), ArticleStatus.PUBLISHED.name()))
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
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED.name())) {
            throw new BusinessException("资讯未发布");
        }

        Long userId = BaseContext.getCurrentId();
        String actionType = voteType == 1 ? ACTION_LIKE : ACTION_DISLIKE;
        String oppositeAction = voteType == 1 ? ACTION_DISLIKE : ACTION_LIKE;

        NewsUserInteraction existing = interactionMapper.getByUserArticleAction(userId, id, actionType);
        NewsUserInteraction opposite = interactionMapper.getByUserArticleAction(userId, id, oppositeAction);

        if (existing != null) {
            // 再次点击同类 → 取消
            interactionMapper.deleteByUserArticleAction(userId, id, actionType);
            changeVoteCount(id, voteType, -1);
        } else {
            if (opposite != null) {
                // 切换：先撤销相反类型
                interactionMapper.deleteByUserArticleAction(userId, id, oppositeAction);
                changeVoteCount(id, voteType == 1 ? -1 : 1, -1);
            }
            NewsUserInteraction interaction = new NewsUserInteraction();
            interaction.setUserId(userId);
            interaction.setArticleId(id);
            interaction.setActionType(actionType);
            interaction.setCreateTime(LocalDateTime.now());
            interaction.setUpdateTime(LocalDateTime.now());
            interactionMapper.insert(interaction);
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
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED.name())) {
            throw new BusinessException("资讯未发布");
        }

        Long userId = BaseContext.getCurrentId();
        NewsUserInteraction existing = interactionMapper.getByUserArticleAction(userId, id, ACTION_FAVORITE);

        if (existing == null) {
            // 新增收藏
            NewsUserInteraction favorite = new NewsUserInteraction();
            favorite.setUserId(userId);
            favorite.setArticleId(id);
            favorite.setActionType(ACTION_FAVORITE);
            favorite.setCreateTime(LocalDateTime.now());
            favorite.setUpdateTime(LocalDateTime.now());
            interactionMapper.insert(favorite);
            articleMapper.incrementFavoriteCount(id, 1);
        } else {
            // 取消收藏
            interactionMapper.deleteByUserArticleAction(userId, id, ACTION_FAVORITE);
            articleMapper.incrementFavoriteCount(id, -1);
        }

        return toVO(getArticle(id), true);
    }

    /**
     * 记录阅读行为（同一用户同一天同一篇只计一次）
     */
    @Override
    public void recordRead(Long id) {
        NewsArticle article = getArticle(id);
        if (!Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED.name())) {
            throw new BusinessException("资讯未发布");
        }

        Long userId = BaseContext.getCurrentId() == null ? 0L : BaseContext.getCurrentId();
        LocalDate today = LocalDate.now();
        Long already = readRecordMapper.countByUserArticleDate(userId, id, today);
        if (already == null || already == 0) {
            NewsReadRecord record = new NewsReadRecord();
            record.setArticleId(id);
            record.setUserId(userId);
            record.setCreateTime(LocalDateTime.now());
            readRecordMapper.insert(record);
            articleMapper.incrementViewCount(id);
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

    /** Entity转VO（填充分类名、作者名、正文、用户投票/收藏状态、付费状态） */
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

        // 付费文章：未登录/未购买时只返预览，购买后返完整；作者本人始终可看完整
        boolean purchased = false;
        boolean isPaid = article.getIsPaid() != null && article.getIsPaid() == 1;
        if (isPaid) {
            Long currentId = BaseContext.getCurrentId();
            boolean isAuthor = currentId != null && Objects.equals(currentId, article.getAuthorId());
            if (isAuthor) {
                purchased = true;
            } else if (currentId != null && JwtClaimsConstant.USER.equals(BaseContext.getCurrentType())) {
                purchased = payOrderMapper.getPaidByUserArticle(currentId, article.getId()) != null;
            }
            vo.setPurchased(purchased);
            if (withContent) {
                if (purchased) {
                    vo.setContent(article.getPaidOnly() != null ? article.getPaidOnly() : article.getContent());
                } else {
                    vo.setContent(article.getPaidPreview());
                }
            }
        } else if (withContent) {
            // 免费文章按原逻辑
            vo.setContent(article.getContent());
        }

        // 填充当前用户的投票和收藏状态
        if (BaseContext.getCurrentId() != null && JwtClaimsConstant.USER.equals(BaseContext.getCurrentType())) {
            NewsUserInteraction like = interactionMapper.getByUserArticleAction(BaseContext.getCurrentId(), article.getId(), ACTION_LIKE);
            NewsUserInteraction dislike = interactionMapper.getByUserArticleAction(BaseContext.getCurrentId(), article.getId(), ACTION_DISLIKE);
            if (like != null) {
                vo.setUserVote(1);
            } else if (dislike != null) {
                vo.setUserVote(-1);
            }
            NewsUserInteraction favorite = interactionMapper.getByUserArticleAction(BaseContext.getCurrentId(), article.getId(), ACTION_FAVORITE);
            vo.setFavorited(favorite != null);
        }

        return vo;
    }

    /** 解析作者名（统一从 sys_user 查询） */
    private String resolveAuthorName(NewsArticle article) {
        SysUser user = sysUserMapper.getById(article.getAuthorId());
        if (user == null) {
            return isUserAuthor(article) ? "用户投稿" : "平台编辑";
        }
        if (isUserAuthor(article)) {
            return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        }
        return StringUtils.hasText(user.getName()) ? user.getName() : user.getUsername();
    }

    /** 判断文章作者是否为普通用户（原 author_type 字段已移除，改为从 sys_user.user_type 反查） */
    private boolean isUserAuthor(NewsArticle article) {
        SysUser user = sysUserMapper.getById(article.getAuthorId());
        return user != null && JwtClaimsConstant.USER.equals(user.getUserType());
    }

    /** 更新稿件状态 */
    private void updateStatus(Long id, String status, String rejectReason) {
        NewsArticle update = new NewsArticle();
        update.setId(id);
        update.setStatus(status);
        update.setRejectReason(rejectReason);
        update.setUpdateTime(LocalDateTime.now());
        update.setUpdateUser(BaseContext.getCurrentId());
        articleMapper.update(update);
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
        if (isUserAuthor(article)) {
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

}
