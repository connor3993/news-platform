package com.connor.newsplatform.server.service.impl;

import com.connor.newsplatform.common.constant.ArticleStatus;
import com.connor.newsplatform.common.constant.CacheKeys;
import com.connor.newsplatform.common.constant.JwtClaimsConstant;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.result.PageResult;
import com.connor.newsplatform.pojo.dto.CategoryDTO;
import com.connor.newsplatform.pojo.entity.NewsCategory;
import com.connor.newsplatform.pojo.vo.CategoryVO;
import com.connor.newsplatform.server.mapper.NewsCategoryMapper;
import com.connor.newsplatform.server.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final NewsCategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public CategoryServiceImpl(NewsCategoryMapper categoryMapper, RedisTemplate<String, Object> redisTemplate) {
        this.categoryMapper = categoryMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 分页查询分类
     */
    @Override
    public PageResult<CategoryVO> page(int page, int pageSize, String name, Integer status) {
        com.github.pagehelper.PageHelper.startPage(page, pageSize);
        com.github.pagehelper.Page<NewsCategory> p = categoryMapper.pageQuery(name, status);
        List<CategoryVO> list = p.getResult().stream()
                .map(entity -> {
                    CategoryVO vo = new CategoryVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        return new PageResult<>(p.getTotal(), list);
    }

    /**
     * 新增分类
     */
    @Override
    @Transactional
    public void create(CategoryDTO dto) {
        // 1. 检查分类名称是否已存在
        Long count = categoryMapper.countByName(dto.getName(), null);
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        // 2. 新增
        NewsCategory category = new NewsCategory();
        BeanUtils.copyProperties(dto, category);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
        // 3. 清除缓存
        redisTemplate.delete(CacheKeys.CATEGORY_LIST);
    }

    /**
     * 修改分类
     */
    @Override
    @Transactional
    public void update(CategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        Long count = categoryMapper.countByName(dto.getName(), dto.getId());
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        NewsCategory category = new NewsCategory();
        BeanUtils.copyProperties(dto, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);
        redisTemplate.delete(CacheKeys.CATEGORY_LIST);
    }

    /**
     * 删除分类
     */
    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
        redisTemplate.delete(CacheKeys.CATEGORY_LIST);
    }

    /**
     * 启用禁用分类
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        NewsCategory category = new NewsCategory();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);
        redisTemplate.delete(CacheKeys.CATEGORY_LIST);
    }

    /**
     * 查询启用的分类列表（带缓存）
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryVO> enabledList() {
        // 1. 先查缓存
        Object cached = redisTemplate.opsForValue().get(CacheKeys.CATEGORY_LIST);
        if (cached instanceof List<?> list) {
            return (List<CategoryVO>) list;
        }
        // 2. 查数据库
        List<NewsCategory> categories = categoryMapper.selectEnabledList();
        List<CategoryVO> list = categories.stream()
                .map(entity -> {
                    CategoryVO vo = new CategoryVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        // 3. 写入缓存
        redisTemplate.opsForValue().set(CacheKeys.CATEGORY_LIST, list, Duration.ofHours(24));
        return list;
    }
}
