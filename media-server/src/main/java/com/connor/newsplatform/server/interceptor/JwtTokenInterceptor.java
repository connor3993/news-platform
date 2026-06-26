package com.connor.newsplatform.server.interceptor;

import com.connor.newsplatform.common.constant.JwtClaimsConstant;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.properties.JwtProperties;
import com.connor.newsplatform.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {
    private final JwtProperties jwtProperties;

    public JwtTokenInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // 从 token 请求头获取令牌
        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            // 用户端部分接口允许不登录访问
            if (isOptionalUserAuth(uri)) {
                return true;
            }
            throw new IllegalArgumentException("未登录或登录已过期");
        }

        // 解析 Token
        Claims claims = JwtUtil.parseJwt(jwtProperties.getSecretKey(), token);

        // 校验 userType 与请求路径是否匹配（防止用户 token 访问管理端接口）
        String userType = claims.get(JwtClaimsConstant.USER_TYPE, String.class);
        String expectedType = uri.startsWith("/admin/") ? JwtClaimsConstant.ADMIN : JwtClaimsConstant.USER;
        if (!expectedType.equals(userType)) {
            throw new IllegalArgumentException("token 类型不匹配");
        }

        // 将用户信息存入线程上下文，供后续业务代码使用
        BaseContext.setCurrentId(claims.get(JwtClaimsConstant.USER_ID, Long.class));
        BaseContext.setCurrentType(userType);
        return true;
    }

    private boolean isOptionalUserAuth(String uri) {
        return uri.startsWith("/user/article/");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrent();
    }
}
