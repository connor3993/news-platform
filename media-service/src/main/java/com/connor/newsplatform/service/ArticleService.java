package com.connor.newsplatform.service;

import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.domain.req.ArticleDTO;
import com.connor.newsplatform.domain.req.AuditDTO;
import com.connor.newsplatform.domain.vo.ArticleVO;

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

    PageResult<ArticleVO> userPage(int page, int pageSize, Long categoryId, String keyword);

    ArticleVO userDetail(Long id);

    PageResult<ArticleVO> userMinePage(int page, int pageSize, Integer status);

    PageResult<ArticleVO> userFavoritePage(int page, int pageSize);

    ArticleVO vote(Long id, Integer voteType);

    ArticleVO toggleFavorite(Long id);

    void recordRead(Long id);
}
