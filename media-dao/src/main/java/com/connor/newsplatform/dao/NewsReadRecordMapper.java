package com.connor.newsplatform.dao;

import com.connor.newsplatform.domain.po.NewsReadRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 阅读记录 Mapper
 */
@Mapper
public interface NewsReadRecordMapper {

    /**
     * 新增阅读记录
     */
    @Insert("insert into news_read_record (user_id, article_id, create_time) values (#{userId}, #{articleId}, #{createTime})")
    void insert(NewsReadRecord record);

    /**
     * 统计某时间段的阅读总数
     */
    @Select("select count(*) from news_read_record where create_time between #{begin} and #{end}")
    Long countByDateRange(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计某用户在某天对某篇文章的阅读记录数（用于阅读去重）
     */
    @Select("select count(*) from news_read_record where user_id = #{userId} and article_id = #{articleId} and date(create_time) = #{date}")
    Long countByUserArticleDate(@Param("userId") Long userId, @Param("articleId") Long articleId, @Param("date") LocalDate date);
}
