package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.NewsArticleVote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface NewsArticleVoteMapper {

    /** 查询用户对某篇文章的投票记录 */
    @Select("select * from news_article_vote where article_id = #{articleId} and user_id = #{userId}")
    NewsArticleVote getByArticleAndUser(@Param("articleId") Long articleId, @Param("userId") Long userId);

    /** 新增投票 */
    void insert(NewsArticleVote vote);

    /** 修改投票 */
    void update(NewsArticleVote vote);

    /** 根据稿件id删除所有投票 */
    @Delete("delete from news_article_vote where article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);
}
