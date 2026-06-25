package com.connor.newsplatform.server.config;

import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.result.Result;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> business(BusinessException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler({JwtException.class, IllegalArgumentException.class})
    public Result<Void> jwt(Exception e) {
        return new Result<>(401, "登录已过期，请重新登录", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> valid(MethodArgumentNotValidException e) {
        return Result.error(e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("参数校验失败"));
    }

    @ExceptionHandler({BindException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public Result<Void> bind(Exception e) {
        return Result.error("请求参数错误");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> upload(MaxUploadSizeExceededException e) {
        return Result.error("上传文件过大");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> duplicate(DuplicateKeyException e) {
        return Result.error("数据已存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> exception(Exception e) {
        return Result.error("系统异常");
    }
}
