package com.connor.newsplatform.server.controller.user;

import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.vo.CategoryVO;
import com.connor.newsplatform.server.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/category")
public class UserCategoryController {
    private final CategoryService categoryService;

    public UserCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.enabledList());
    }
}
