package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.NewsArticleFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 新闻收藏 Mapper
 */
@Mapper
public interface NewsArticleFavoriteMapper {

    /**
     * 分页查询用户收藏列表
     */
    @Select("select * from news_article_favorite where user_id = #{userId} order by create_time desc")
    Page<NewsArticleFavorite> pageByUserId(@Param("userId") Long userId);

    /**
     * 查询用户是否已收藏某篇文章
     */
    @Select("select * from news_article_favorite where user_id = #{userId} and article_id = #{articleId}")
    NewsArticleFavorite getByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * 新增收藏记录
     */
    @Insert("insert into news_article_favorite (user_id, article_id, create_time) values (#{userId}, #{articleId}, #{createTime})")
    void insert(NewsArticleFavorite favorite);

    /**
     * 根据id删除收藏记录
     */
    @Delete("delete from news_article_favorite where id = #{id}")
    void deleteById(@Param("id") Long id);

    /**
     * 根据稿件id删除所有收藏记录
     */
    @Delete("delete from news_article_favorite where article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);
}
