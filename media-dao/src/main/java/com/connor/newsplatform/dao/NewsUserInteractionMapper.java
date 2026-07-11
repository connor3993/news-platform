package com.connor.newsplatform.dao;

import com.connor.newsplatform.domain.po.NewsUserInteraction;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户互动 Mapper（合并原 news_article_vote / news_article_favorite）
 */
@Mapper
public interface NewsUserInteractionMapper {

    /**
     * 查询用户对某篇文章的某类互动记录
     */
    @Select("select * from news_user_interaction where user_id = #{userId} and article_id = #{articleId} and action_type = #{actionType}")
    NewsUserInteraction getByUserArticleAction(@Param("userId") Long userId,
                                               @Param("articleId") Long articleId,
                                               @Param("actionType") String actionType);

    /**
     * 统计某用户对某篇文章的某类互动数量
     */
    @Select("select count(*) from news_user_interaction where user_id = #{userId} and article_id = #{articleId} and action_type = #{actionType}")
    Integer countByUserArticleAction(@Param("userId") Long userId,
                                     @Param("articleId") Long articleId,
                                     @Param("actionType") String actionType);

    /**
     * 分页查询某用户的某类互动（用于收藏列表）
     */
    @Select("select * from news_user_interaction where user_id = #{userId} and action_type = #{actionType} order by create_time desc")
    Page<NewsUserInteraction> pageByUserAndAction(@Param("userId") Long userId, @Param("actionType") String actionType);

    /**
     * 新增互动记录
     */
    @Insert("insert into news_user_interaction (user_id, article_id, action_type, create_time, update_time) " +
            "values (#{userId}, #{articleId}, #{actionType}, #{createTime}, #{updateTime})")
    void insert(NewsUserInteraction interaction);

    /**
     * 删除某用户对某篇文章的某类互动
     */
    @Delete("delete from news_user_interaction where user_id = #{userId} and article_id = #{articleId} and action_type = #{actionType}")
    void deleteByUserArticleAction(@Param("userId") Long userId,
                                   @Param("articleId") Long articleId,
                                   @Param("actionType") String actionType);

    /**
     * 根据稿件id删除所有互动记录
     */
    @Delete("delete from news_user_interaction where article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);
}
