package com.connor.newsplatform.web.config;

import com.connor.newsplatform.service.weixin.IWeixinApiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * 微信公众号 HTTP 客户端（Retrofit）
 */
@Configuration
public class Retrofit2Config {

    private static final String BASE_URL = "https://api.weixin.qq.com";

    @Bean
    public Retrofit retrofit() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create()).build();
    }

    @Bean
    public IWeixinApiService weixinApiService(Retrofit retrofit) {
        return retrofit.create(IWeixinApiService.class);
    }
}
