package com.connor.newsplatform.web.config;

import com.connor.newsplatform.web.interceptor.JwtTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtTokenInterceptor jwtTokenInterceptor;

    public WebMvcConfig(JwtTokenInterceptor jwtTokenInterceptor) {
        this.jwtTokenInterceptor = jwtTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 公共接口白名单（微信/支付宝回调、微信登录等，不需要 JWT）
        String[] publicPaths = {
                "/api/v1/weixin/**",
                "/api/v1/alipay/**",
                "/api/v1/login/**"
        };
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login")
                .excludePathPatterns(publicPaths);
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                        "/user/auth/register",
                        "/user/auth/login",
                        "/user/category/list",
                        "/user/article/page"
                )
                .excludePathPatterns(publicPaths);
        // /common/** 需要登录，admin/user 均可（由拦截器内部判断 userType）
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/common/**")
                .excludePathPatterns(publicPaths);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
