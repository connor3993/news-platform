package com.connor.newsplatform.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "alipay", ignoreInvalidFields = true)
public class AliPayConfigProperties {

    // 「沙箱环境」应用ID
    private String app_id;
    // 「沙箱环境」商户私钥（PKCS8 格式 RSA2）
    private String merchant_private_key;
    // 「沙箱环境」支付宝公钥
    private String alipay_public_key;
    // 「沙箱环境」服务器异步通知页面路径（内网穿透公网地址）
    private String notify_url;
    // 「沙箱环境」页面跳转同步通知页面路径
    private String return_url;
    // 「沙箱环境」网关
    private String gatewayUrl;
    // 签名方式
    private String sign_type = "RSA2";
    // 字符编码格式
    private String charset = "utf-8";
    // 传输格式
    private String format = "json";
}
