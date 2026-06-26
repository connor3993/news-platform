package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.NewsArticleContent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 新闻稿件正文 Mapper
 */
@Mapper
public interface NewsArticleContentMapper {

    /**
     * 根据稿件id查询正文
     */
    @Select("select * from news_article_content where article_id = #{articleId}")
    NewsArticleContent getByArticleId(@Param("articleId") Long articleId);

    /**
     * 新增正文
     */
    @Insert("insert into news_article_content (article_id, content) values (#{articleId}, #{content})")
    void insert(NewsArticleContent content);

    /**
     * 根据稿件id删除正文
     */
    @org.apache.ibatis.annotations.Delete("delete from news_article_content where article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);
}
