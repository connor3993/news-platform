package com.connor.newsplatform.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 付费/支付相关配置。
 * mock=true 走平台内 mock 支付；mock=false 留作真沙箱（需自行接入，本项目不内置）。
 */
@Data
@ConfigurationProperties(prefix = "media.pay")
public class PayProperties {
    /** 是否走 mock 模式（推荐开启，无需沙箱账号） */
    private boolean mock = true;
    /** 未支付订单超时关单时间（分钟） */
    private int timeoutMinutes = 30;
}
