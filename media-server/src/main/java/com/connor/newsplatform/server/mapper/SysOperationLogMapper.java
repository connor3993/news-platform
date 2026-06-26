package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.SysOperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 系统操作日志 Mapper
 */
@Mapper
public interface SysOperationLogMapper {

    /**
     * 分页查询操作日志（支持操作类型和时间范围筛选）
     */
    Page<SysOperationLog> pageQuery(
            @Param("operation") String operation,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 新增操作日志
     */
    @Insert("insert into sys_operation_log " +
            "(operator_id, operator_type, operation, request_uri, request_method, request_params, ip, create_time) " +
            "values (#{operatorId}, #{operatorType}, #{operation}, #{requestUri}, #{requestMethod}, #{requestParams}, #{ip}, #{createTime})")
    void insert(SysOperationLog log);
}
