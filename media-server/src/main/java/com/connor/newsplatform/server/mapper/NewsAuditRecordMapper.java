package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.NewsAuditRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 新闻审核记录 Mapper
 */
@Mapper
public interface NewsAuditRecordMapper {

    /**
     * 新增审核记录
     */
    @Insert("insert into news_audit_record (article_id, auditor_id, audit_status, audit_comment, create_time) " +
            "values (#{articleId}, #{auditorId}, #{auditStatus}, #{auditComment}, #{createTime})")
    void insert(NewsAuditRecord record);

    /**
     * 根据稿件id删除所有审核记录
     */
    @Delete("delete from news_audit_record where article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 统计某时间段的审核数
     */
    @Select("select count(*) from news_audit_record where create_time between #{begin} and #{end}")
    Integer countByDateRange(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计某时间段驳回数（audit_status=3表示驳回）
     */
    @Select("select count(*) from news_audit_record where audit_status = 3 and create_time between #{begin} and #{end}")
    Integer countRejectedByDateRange(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
