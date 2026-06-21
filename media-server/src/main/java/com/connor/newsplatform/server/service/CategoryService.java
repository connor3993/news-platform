package com.connor.newsplatform.server.service;

import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.dto.CategoryDTO;
import com.connor.newsplatform.pojo.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    PageResult<CategoryVO> page(int page, int pageSize, String name, Integer status);

    void create(CategoryDTO dto);

    void update(CategoryDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    List<CategoryVO> enabledList();
}
