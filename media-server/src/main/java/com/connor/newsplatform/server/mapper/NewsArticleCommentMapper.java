package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.NewsArticleComment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 新闻评论 Mapper
 */
@Mapper
public interface NewsArticleCommentMapper {

    /**
     * 分页查询文章评论（status=1正常）
     */
    @Select("select * from news_article_comment where article_id = #{articleId} and status = 1 order by create_time desc")
    Page<NewsArticleComment> pageByArticleId(@Param("articleId") Long articleId);

    /**
     * 新增评论
     */
    @Insert("insert into news_article_comment (article_id, user_id, content, status, create_time) " +
            "values (#{articleId}, #{userId}, #{content}, #{status}, #{createTime})")
    void insert(NewsArticleComment comment);

    /**
     * 根据稿件id删除所有评论
     */
    @org.apache.ibatis.annotations.Delete("delete from news_article_comment where article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 根据用户id批量查询用户评论信息
     */
    List<NewsArticleComment> selectByUserIds(@Param("userIds") List<Long> userIds);
}
