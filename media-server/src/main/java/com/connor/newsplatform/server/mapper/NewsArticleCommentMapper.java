package com.connor.newsplatform.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.connor.newsplatform.pojo.entity.NewsArticleComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NewsArticleCommentMapper extends BaseMapper<NewsArticleComment> {
}
