package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.NewsArticle;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻稿件 Mapper
 */
@Mapper
public interface NewsArticleMapper {

    /**
     * 管理端分页查询（支持标题/分类/状态/时间范围筛选）
     */
    Page<NewsArticle> adminPageQuery(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 用户端分页查询（仅已发布稿件，支持分类和关键词搜索，支持排序方式）
     */
    Page<NewsArticle> userPageQuery(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("sort") String sort);

    /**
     * 用户我的稿件分页查询（按作者和状态筛选）
     */
    Page<NewsArticle> userMinePageQuery(
            @Param("authorId") Long authorId,
            @Param("status") Integer status);

    /**
     * 根据id查询稿件详情
     */
    @Select("select * from news_article where id = #{id}")
    NewsArticle getById(@Param("id") Long id);

    /**
     * 新增稿件
     */
    void insert(NewsArticle article);

    /**
     * 修改稿件信息（动态SQL，只更新非空字段）
     */
    void update(NewsArticle article);

    /**
     * 根据id删除稿件
     */
    @Delete("delete from news_article where id = #{id}")
    void deleteById(@Param("id") Long id);

    /**
     * 根据id列表批量查询稿件
     */
    List<NewsArticle> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 查询所有已发布稿件（用于定时任务热度计算）
     */
    @Select("select * from news_article where status = 4")
    List<NewsArticle> selectAllPublished();

    /**
     * 热点榜单 top10（支持多种排序方式：view/like/popular/hot_score）
     */
    List<NewsArticle> selectHotList(@Param("sort") String sort);

    /**
     * 更新稿件热度分
     */
    @Update("update news_article set hot_score = #{hotScore} where id = #{id}")
    void updateHotScore(@Param("id") Long id, @Param("hotScore") BigDecimal hotScore);

    /**
     * 阅读数+1，同时重新计算热度分
     */
    @Update("update news_article " +
            "set view_count = view_count + 1, " +
            "hot_score = view_count + 1 + like_count * 4 + comment_count * 3 + favorite_count * 3 - dislike_count " +
            "where id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 点赞数增减（防止小于0）
     */
    @Update("update news_article set like_count = GREATEST(like_count + #{delta}, 0) where id = #{id}")
    void incrementLikeCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 点踩数增减（防止小于0）
     */
    @Update("update news_article set dislike_count = GREATEST(dislike_count + #{delta}, 0) where id = #{id}")
    void incrementDislikeCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 收藏数增减（防止小于0）
     */
    @Update("update news_article set favorite_count = GREATEST(favorite_count + #{delta}, 0) where id = #{id}")
    void incrementFavoriteCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 评论数增减（防止小于0）
     */
    @Update("update news_article set comment_count = GREATEST(comment_count + #{delta}, 0) where id = #{id}")
    void incrementCommentCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 统计某状态在某时间段内发布的稿件数
     */
    @Select("select count(*) from news_article where status = #{status} and publish_time between #{begin} and #{end}")
    Integer countByStatusAndPublishTime(@Param("status") Integer status, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计某时间段内新增的稿件数
     */
    @Select("select count(*) from news_article where create_time between #{begin} and #{end}")
    Integer countByCreateTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
