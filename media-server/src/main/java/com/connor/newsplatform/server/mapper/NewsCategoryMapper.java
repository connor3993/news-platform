package com.connor.newsplatform.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.connor.newsplatform.pojo.entity.NewsCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NewsCategoryMapper extends BaseMapper<NewsCategory> {
}
