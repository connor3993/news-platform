package com.connor.newsplatform.server.service;

import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.dto.ArticleDTO;
import com.connor.newsplatform.pojo.dto.AuditDTO;
import com.connor.newsplatform.pojo.dto.CommentDTO;
import com.connor.newsplatform.pojo.vo.ArticleVO;
import com.connor.newsplatform.pojo.vo.CommentVO;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleService {
    PageResult<ArticleVO> adminPage(int page, int pageSize, String title, Long categoryId, Integer status,
                                    LocalDateTime beginTime, LocalDateTime endTime);

    ArticleVO adminDetail(Long id);

    Long createDraft(ArticleDTO dto);

    void submitUserArticle(ArticleDTO dto);

    void updateArticle(ArticleDTO dto);

    void deleteArticle(Long id);

    void submit(Long id);

    void approve(Long id, AuditDTO dto);

    void reject(Long id, AuditDTO dto);

    void publish(Long id);

    void offline(Long id);

    PageResult<ArticleVO> userPage(int page, int pageSize, Long categoryId, String keyword, String sort);

    ArticleVO userDetail(Long id);

    PageResult<ArticleVO> userMinePage(int page, int pageSize, Integer status);

    PageResult<ArticleVO> userFavoritePage(int page, int pageSize);

    ArticleVO vote(Long id, Integer voteType);

    ArticleVO toggleFavorite(Long id);

    PageResult<CommentVO> commentPage(Long id, int page, int pageSize);

    CommentVO addComment(Long id, CommentDTO dto);

    void recordRead(Long id);

    List<ArticleVO> hotList(String sort);

    void recalculateHotScore();
}
