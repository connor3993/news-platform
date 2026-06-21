package com.connor.newsplatform.server.controller.admin;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.dto.ArticleDTO;
import com.connor.newsplatform.pojo.dto.AuditDTO;
import com.connor.newsplatform.pojo.vo.ArticleVO;
import com.connor.newsplatform.server.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/article")
public class AdminArticleController {
    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/page")
    public Result<PageResult<ArticleVO>> page(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize,
                                              String title,
                                              Long categoryId,
                                              Integer status,
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(articleService.adminPage(page, pageSize, title, categoryId, status, beginTime, endTime));
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.adminDetail(id));
    }

    @LogRecord("新增稿件草稿")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ArticleDTO dto) {
        return Result.success(articleService.createDraft(dto));
    }

    @LogRecord("修改稿件")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ArticleDTO dto) {
        articleService.updateArticle(dto);
        return Result.success();
    }

    @LogRecord("删除稿件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @LogRecord("提交审核")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        articleService.submit(id);
        return Result.success();
    }

    @LogRecord("审核通过")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) AuditDTO dto) {
        articleService.approve(id, dto);
        return Result.success();
    }

    @LogRecord("审核驳回")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) AuditDTO dto) {
        articleService.reject(id, dto);
        return Result.success();
    }

    @LogRecord("发布稿件")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.publish(id);
        return Result.success();
    }

    @LogRecord("下架稿件")
    @PostMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        articleService.offline(id);
        return Result.success();
    }
}
