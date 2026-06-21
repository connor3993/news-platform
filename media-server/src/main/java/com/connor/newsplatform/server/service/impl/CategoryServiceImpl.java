package com.connor.newsplatform.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.connor.newsplatform.common.constant.CacheKeys;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.dto.CategoryDTO;
import com.connor.newsplatform.pojo.entity.NewsCategory;
import com.connor.newsplatform.pojo.vo.CategoryVO;
import com.connor.newsplatform.server.mapper.NewsCategoryMapper;
import com.connor.newsplatform.server.service.CategoryService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final NewsCategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public CategoryServiceImpl(NewsCategoryMapper categoryMapper, RedisTemplate<String, Object> redisTemplate) {
        this.categoryMapper = categoryMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public PageResult<CategoryVO> page(int page, int pageSize, String name, Integer status) {
        Page<NewsCategory> result = categoryMapper.selectPage(Page.of(page, pageSize), new LambdaQueryWrapper<NewsCategory>()
                .like(StringUtils.hasText(name), NewsCategory::getName, name)
                .eq(status != null, NewsCategory::getStatus, status)
                .orderByAsc(NewsCategory::getSort)
                .orderByDesc(NewsCategory::getCreateTime));
        return new PageResult<>(result.getTotal(), result.getRecords().stream()
                .map(entity -> BeanCopy.to(entity, CategoryVO.class))
                .toList());
    }

    @Override
    @Transactional
    public void create(CategoryDTO dto) {
        ensureNameUnique(dto.getName(), null);
        NewsCategory category = BeanCopy.to(dto, NewsCategory.class);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
        clearCache();
    }

    @Override
    @Transactional
    public void update(CategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        ensureNameUnique(dto.getName(), dto.getId());
        NewsCategory category = BeanCopy.to(dto, NewsCategory.class);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateById(category);
        clearCache();
    }

    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
        clearCache();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        NewsCategory category = new NewsCategory();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateById(category);
        clearCache();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryVO> enabledList() {
        Object cached = getCachedValue(CacheKeys.CATEGORY_LIST);
        if (cached instanceof List<?> list) {
            return (List<CategoryVO>) list;
        }
        List<CategoryVO> categories = new ArrayList<>(categoryMapper.selectList(new LambdaQueryWrapper<NewsCategory>()
                        .eq(NewsCategory::getStatus, 1)
                        .orderByAsc(NewsCategory::getSort))
                .stream()
                .map(entity -> BeanCopy.to(entity, CategoryVO.class))
                .toList());
        redisTemplate.opsForValue().set(CacheKeys.CATEGORY_LIST, categories, Duration.ofHours(24));
        return categories;
    }

    private void ensureNameUnique(String name, Long currentId) {
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<NewsCategory>()
                .eq(NewsCategory::getName, name)
                .ne(currentId != null, NewsCategory::getId, currentId));
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
    }

    private void clearCache() {
        redisTemplate.delete(CacheKeys.CATEGORY_LIST);
    }

    private Object getCachedValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RuntimeException ex) {
            redisTemplate.delete(key);
            return null;
        }
    }
}
