package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.NewsDailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface NewsDailyStatsMapper {

    /** 查询日期范围内的统计 */
    @Select("select * from news_daily_stats where stat_date between #{begin} and #{end} order by stat_date asc")
    List<NewsDailyStats> selectByDateRange(@Param("begin") LocalDate begin, @Param("end") LocalDate end);

    /** 删除某天的统计 */
    @Delete("delete from news_daily_stats where stat_date = #{date}")
    void deleteByDate(@Param("date") LocalDate date);

    /** 新增统计 */
    void insert(NewsDailyStats stats);
}
