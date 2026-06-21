package com.connor.newsplatform.server.controller.user;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.dto.ArticleDTO;
import com.connor.newsplatform.pojo.vo.ArticleVO;
import com.connor.newsplatform.server.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/article")
public class UserArticleController {
    private final ArticleService articleService;

    public UserArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/page")
    public Result<PageResult<ArticleVO>> page(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize,
                                              Long categoryId,
                                              String keyword,
                                              String sort) {
        return Result.success(articleService.userPage(page, pageSize, categoryId, keyword, sort));
    }

    @GetMapping("/hot")
    public Result<List<ArticleVO>> hot() {
        return Result.success(articleService.hotList());
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.userDetail(id));
    }

    @GetMapping("/mine/page")
    public Result<PageResult<ArticleVO>> mine(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize,
                                              Integer status) {
        return Result.success(articleService.userMinePage(page, pageSize, status));
    }

    @LogRecord("用户投稿")
    @PostMapping
    public Result<Void> submitArticle(@Valid @RequestBody ArticleDTO dto) {
        articleService.submitUserArticle(dto);
        return Result.success();
    }

    @LogRecord("文章点赞")
    @PostMapping("/{id}/like")
    public Result<ArticleVO> like(@PathVariable Long id) {
        return Result.success(articleService.vote(id, 1));
    }

    @LogRecord("文章点踩")
    @PostMapping("/{id}/dislike")
    public Result<ArticleVO> dislike(@PathVariable Long id) {
        return Result.success(articleService.vote(id, -1));
    }

    @LogRecord("记录阅读行为")
    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        articleService.recordRead(id);
        return Result.success();
    }
}
