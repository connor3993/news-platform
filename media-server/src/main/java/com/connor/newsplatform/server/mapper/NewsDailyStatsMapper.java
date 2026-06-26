package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.NewsDailyStats;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日统计 Mapper
 */
@Mapper
public interface NewsDailyStatsMapper {

    /**
     * 查询日期范围内的统计数据
     */
    @Select("select * from news_daily_stats where stat_date between #{begin} and #{end} order by stat_date asc")
    List<NewsDailyStats> selectByDateRange(@Param("begin") LocalDate begin, @Param("end") LocalDate end);

    /**
     * 删除某天的统计记录
     */
    @Delete("delete from news_daily_stats where stat_date = #{date}")
    void deleteByDate(@Param("date") LocalDate date);

    /**
     * 新增每日统计
     */
    @Insert("insert into news_daily_stats " +
            "(stat_date, article_count, publish_count, view_count, audit_count, reject_count, create_time) " +
            "values (#{statDate}, #{articleCount}, #{publishCount}, #{viewCount}, #{auditCount}, #{rejectCount}, #{createTime})")
    void insert(NewsDailyStats stats);
}
