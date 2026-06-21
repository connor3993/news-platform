package com.connor.newsplatform.server.service;

import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.dto.ArticleDTO;
import com.connor.newsplatform.pojo.dto.AuditDTO;
import com.connor.newsplatform.pojo.vo.ArticleVO;

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

    ArticleVO vote(Long id, Integer voteType);

    void recordRead(Long id);

    List<ArticleVO> hotList();

    void recalculateHotScore();
}
