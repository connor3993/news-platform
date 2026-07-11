package com.connor.newsplatform.dao;

import com.github.pagehelper.Page;
import com.connor.newsplatform.domain.po.NewsArticle;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
     * 用户端分页查询（仅已发布稿件，支持分类和关键词搜索）
     */
    Page<NewsArticle> userPageQuery(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword);

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
    @Select("select * from news_article where status = 'PUBLISHED'")
    List<NewsArticle> selectAllPublished();

    /**
     * 阅读数+1
     */
    @Update("update news_article set view_count = view_count + 1 where id = #{id}")
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
     * 统计某状态在某时间段内发布的稿件数
     */
    @Select("select count(*) from news_article where status = #{status} and publish_time between #{begin} and #{end}")
    Integer countByStatusAndPublishTime(@Param("status") String status, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计某时间段内新增的稿件数
     */
    @Select("select count(*) from news_article where create_time between #{begin} and #{end}")
    Integer countByCreateTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计某状态在某时间段内（按更新时间）的稿件数（用于审核相关统计）
     */
    @Select("select count(*) from news_article where status = #{status} and update_time between #{begin} and #{end}")
    Integer countByStatusAndUpdateTime(@Param("status") String status, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
