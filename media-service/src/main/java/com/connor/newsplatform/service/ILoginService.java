package com.connor.newsplatform.service;

import com.connor.newsplatform.domain.vo.LoginVO;

import java.io.IOException;

/**
 * 微信扫码登录服务
 */
public interface ILoginService {

    /**
     * 生成微信扫码登录二维码 ticket
     */
    String createQrCodeTicket() throws Exception;

    /**
     * 根据 ticket 校验登录态；未扫码返回 null，已扫码返回登录凭证（LoginVO + JWT）
     */
    LoginVO checkLogin(String ticket);

    /**
     * 用户扫码后，保存 ticket -> openid 的登录态，并下发模板消息
     */
    void saveLoginState(String ticket, String openid) throws IOException;

}
