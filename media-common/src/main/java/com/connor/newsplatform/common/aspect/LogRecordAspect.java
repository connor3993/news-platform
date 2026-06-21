package com.connor.newsplatform.common.aspect;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.context.BaseContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogRecordAspect {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectMapper objectMapper;

    public LogRecordAspect(ObjectProvider<JdbcTemplate> jdbcTemplateProvider, ObjectMapper objectMapper) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.objectMapper = objectMapper;
    }

    @Pointcut("@annotation(com.connor.newsplatform.common.annotation.LogRecord)")
    public void logPointcut() {
    }

    @AfterReturning("logPointcut() && @annotation(logRecord)")
    public void record(JoinPoint joinPoint, LogRecord logRecord) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (jdbcTemplate == null || attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        try {
            String params = objectMapper.writeValueAsString(Arrays.stream(joinPoint.getArgs())
                    .filter(arg -> !(arg instanceof MultipartFile))
                    .toList());
            jdbcTemplate.update(
                    "INSERT INTO sys_operation_log "
                            + "(operator_id, operator_type, operation, request_uri, request_method, request_params, ip, create_time) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    BaseContext.getCurrentId(),
                    BaseContext.getCurrentType(),
                    logRecord.value(),
                    request.getRequestURI(),
                    request.getMethod(),
                    params,
                    request.getRemoteAddr(),
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.warn("record operation log failed", e);
        }
    }
}
