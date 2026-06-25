package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.NewsArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NewsArticleMapper {

    /** 管理端分页查询 */
    Page<NewsArticle> adminPageQuery(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime);

    /** 用户端分页查询（已发布） */
    Page<NewsArticle> userPageQuery(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("sort") String sort);

    /** 用户我的稿件分页查询 */
    Page<NewsArticle> userMinePageQuery(
            @Param("authorId") Long authorId,
            @Param("status") Integer status);

    /** 根据id查询稿件 */
    NewsArticle getById(@Param("id") Long id);

    /** 新增稿件 */
    void insert(NewsArticle article);

    /** 修改稿件（动态SQL） */
    void update(NewsArticle article);

    /** 删除稿件 */
    void deleteById(@Param("id") Long id);

    /** 根据id列表批量查询 */
    List<NewsArticle> selectByIds(@Param("ids") List<Long> ids);

    /** 查询所有已发布稿件（用于热度计算） */
    List<NewsArticle> selectAllPublished();

    /** 热点列表 top10 */
    List<NewsArticle> selectHotList(@Param("sort") String sort);

    /** 更新热度分 */
    void updateHotScore(@Param("id") Long id, @Param("hotScore") BigDecimal hotScore);

    /** 阅读数+1，同时更新热度分 */
    void incrementViewCount(@Param("id") Long id);

    /** 点赞数增减 */
    void incrementLikeCount(@Param("id") Long id, @Param("delta") int delta);

    /** 点踩数增减 */
    void incrementDislikeCount(@Param("id") Long id, @Param("delta") int delta);

    /** 收藏数增减 */
    void incrementFavoriteCount(@Param("id") Long id, @Param("delta") int delta);

    /** 评论数增减 */
    void incrementCommentCount(@Param("id") Long id, @Param("delta") int delta);

    /** 统计某状态某时间段新增的稿件数 */
    @org.apache.ibatis.annotations.Select("select count(*) from news_article where status = #{status} and publish_time between #{begin} and #{end}")
    Integer countByStatusAndPublishTime(@Param("status") Integer status, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /** 统计某时间段新增的稿件数 */
    @org.apache.ibatis.annotations.Select("select count(*) from news_article where create_time between #{begin} and #{end}")
    Integer countByCreateTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
