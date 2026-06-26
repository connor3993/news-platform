package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.NewsReadRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
