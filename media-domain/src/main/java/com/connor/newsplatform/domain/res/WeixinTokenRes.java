package com.connor.newsplatform.domain.res;

import lombok.Data;

/**
 * 微信获取 access_token 响应
 */
@Data
public class WeixinTokenRes {

    private String access_token;
    private int expires_in;
    private String errcode;
    private String errmsg;

}
