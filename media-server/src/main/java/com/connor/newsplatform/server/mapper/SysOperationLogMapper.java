package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysOperationLogMapper {

    /** 分页查询操作日志 */
    Page<SysOperationLog> pageQuery(
            @Param("operation") String operation,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime);

    /** 新增日志 */
    void insert(SysOperationLog log);
}
