package com.connor.newsplatform.domain.res;

import lombok.Data;

/**
 * 微信创建二维码 ticket 响应
 */
@Data
public class WeixinQrCodeRes {

    private String ticket;
    private Long expire_seconds;
    private String url;

}
