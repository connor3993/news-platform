package com.connor.newsplatform.service;

import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.domain.req.CategoryDTO;
import com.connor.newsplatform.domain.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    PageResult<CategoryVO> page(int page, int pageSize, String name, Integer status);

    void create(CategoryDTO dto);

    void update(CategoryDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    List<CategoryVO> enabledList();
}
