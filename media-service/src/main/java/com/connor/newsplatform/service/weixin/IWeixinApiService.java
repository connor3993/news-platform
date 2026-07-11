package com.connor.newsplatform.service.weixin;

import com.connor.newsplatform.domain.req.WeixinQrCodeReq;
import com.connor.newsplatform.domain.res.WeixinQrCodeRes;
import com.connor.newsplatform.domain.res.WeixinTokenRes;
import com.connor.newsplatform.domain.vo.WeixinTemplateMessageVO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 微信公众号对接服务 retrofit 客户端接口
 */
public interface IWeixinApiService {

    /**
     * 获取微信公众号 access_token
     */
    @GET("cgi-bin/token")
    Call<WeixinTokenRes> getToken(@Query("grant_type") String grantType,
                                  @Query("appid") String appId,
                                  @Query("secret") String appSecret);

    /**
     * 创建二维码 ticket
     */
    @POST("cgi-bin/qrcode/create")
    Call<WeixinQrCodeRes> createQrCode(@Query("access_token") String accessToken, @Body WeixinQrCodeReq req);

    /**
     * 发送模板消息（扫码登录成功提醒）
     */
    @POST("cgi-bin/message/template/send")
    Call<Void> sendMessage(@Query("access_token") String accessToken, @Body WeixinTemplateMessageVO weixinTemplateMessageVO);

}
