package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.NewsArticleComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsArticleCommentMapper {

    /** 分页查询文章评论（status=1正常） */
    Page<NewsArticleComment> pageByArticleId(@Param("articleId") Long articleId);

    /** 新增评论 */
    void insert(NewsArticleComment comment);

    /** 根据稿件id删除所有评论 */
    @org.apache.ibatis.annotations.Delete("delete from news_article_comment where article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);

    /** 根据用户id批量查询用户信息 */
    List<NewsArticleComment> selectByUserIds(@Param("userIds") List<Long> userIds);
}
