package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.NewsArticleVote;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 新闻投票（点赞/点踩） Mapper
 */
@Mapper
public interface NewsArticleVoteMapper {

    /**
     * 查询用户对某篇文章的投票记录
     */
    @Select("select * from news_article_vote where article_id = #{articleId} and user_id = #{userId}")
    NewsArticleVote getByArticleAndUser(@Param("articleId") Long articleId, @Param("userId") Long userId);

    /**
     * 新增投票记录
     */
    @Insert("insert into news_article_vote (user_id, article_id, vote_type, create_time, update_time) " +
            "values (#{userId}, #{articleId}, #{voteType}, #{createTime}, #{updateTime})")
    void insert(NewsArticleVote vote);

    /**
     * 修改投票类型
     */
    @Update("update news_article_vote set vote_type = #{voteType}, update_time = #{updateTime} where id = #{id}")
    void update(NewsArticleVote vote);

    /**
     * 根据稿件id删除所有投票记录
     */
    @Delete("delete from news_article_vote where article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);
}
