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
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        boolean admin = uri.startsWith("/admin/");
        String token = admin ? request.getHeader("token") : request.getHeader("authentication");
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }
        String secret = admin ? jwtProperties.getAdminSecretKey() : jwtProperties.getUserSecretKey();
        Claims claims = JwtUtil.parseJwt(secret, token);
        String expectedType = admin ? JwtClaimsConstant.ADMIN : JwtClaimsConstant.USER;
        if (!expectedType.equals(claims.get(JwtClaimsConstant.USER_TYPE, String.class))) {
            throw new IllegalArgumentException("token 类型不匹配");
        }
        BaseContext.setCurrentId(claims.get(JwtClaimsConstant.USER_ID, Long.class));
        BaseContext.setCurrentType(expectedType);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrent();
    }
}
