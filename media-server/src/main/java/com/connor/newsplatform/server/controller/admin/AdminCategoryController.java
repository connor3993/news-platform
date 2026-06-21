package com.connor.newsplatform.server.controller.admin;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.dto.CategoryDTO;
import com.connor.newsplatform.pojo.vo.CategoryVO;
import com.connor.newsplatform.server.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/category")
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/page")
    public Result<PageResult<CategoryVO>> page(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               String name,
                                               Integer status) {
        return Result.success(categoryService.page(page, pageSize, name, status));
    }

    @LogRecord("新增分类")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryDTO dto) {
        categoryService.create(dto);
        return Result.success();
    }

    @LogRecord("修改分类")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody CategoryDTO dto) {
        categoryService.update(dto);
        return Result.success();
    }

    @LogRecord("删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @LogRecord("启用禁用分类")
    @PostMapping("/status/{status}")
    public Result<Void> status(@PathVariable Integer status, @RequestParam Long id) {
        categoryService.updateStatus(id, status);
        return Result.success();
    }
}
