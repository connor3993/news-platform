package com.connor.newsplatform.service.impl;

import com.connor.newsplatform.common.constant.JwtClaimsConstant;
import com.connor.newsplatform.common.properties.JwtProperties;
import com.connor.newsplatform.common.utils.JwtUtil;
import com.connor.newsplatform.dao.SysUserMapper;
import com.connor.newsplatform.domain.po.SysUser;
import com.connor.newsplatform.domain.req.WeixinQrCodeReq;
import com.connor.newsplatform.domain.res.WeixinQrCodeRes;
import com.connor.newsplatform.domain.res.WeixinTokenRes;
import com.connor.newsplatform.domain.vo.LoginVO;
import com.connor.newsplatform.domain.vo.WeixinTemplateMessageVO;
import com.connor.newsplatform.service.ILoginService;
import com.connor.newsplatform.service.weixin.IWeixinApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信扫码登录服务实现：
 * 1. 生成二维码 ticket（对接公众号）
 * 2. 轮询校验 ticket 对应的登录态
 * 3. 用户扫码后绑定 openid 到 sys_user（首次自动注册），签发 JWT
 */
@Service
public class WeixinLoginServiceImpl implements ILoginService {

    private static final String ACCESS_TOKEN_KEY_PREFIX = "media:weixin:access-token:";
    private static final String OPENID_KEY_PREFIX = "media:weixin:login:openid:";

    @Value("${weixin.config.app-id}")
    private String appid;
    @Value("${weixin.config.app-secret}")
    private String appSecret;
    @Value("${weixin.config.template_id}")
    private String templateId;

    @Resource
    private IWeixinApiService weixinApiService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private JwtProperties jwtProperties;

    @Override
    public String createQrCodeTicket() throws Exception {
        // 1. 获取 accessToken
        String accessToken = getAccessToken();

        // 2. 生成临时二维码 ticket
        WeixinQrCodeReq weixinQrCodeReq = WeixinQrCodeReq.builder()
                .expire_seconds(2592000) // 30天
                .action_name(WeixinQrCodeReq.ActionNameTypeVO.QR_SCENE.getCode())
                .action_info(WeixinQrCodeReq.ActionInfo.builder()
                        .scene(WeixinQrCodeReq.ActionInfo.Scene.builder()
                                .scene_id(100601)
                                .build())
                        .build())
                .build();

        Call<WeixinQrCodeRes> call = weixinApiService.createQrCode(accessToken, weixinQrCodeReq);
        WeixinQrCodeRes weixinQrCodeRes = call.execute().body();
        assert null != weixinQrCodeRes;
        return weixinQrCodeRes.getTicket();
    }

    @Override
    public LoginVO checkLogin(String ticket) {
        // 未扫码：ticket 还未绑定 openid
        String openid = stringRedisTemplate.opsForValue().get(openidKey(ticket));
        if (openid == null || openid.isBlank()) {
            return null;
        }

        // 绑定 openid 到本平台用户（首次扫码自动注册）
        SysUser user = sysUserMapper.getByOpenid(openid);
        if (user == null) {
            user = autoRegister(openid);
        }

        // 签发与本平台一致的 JWT，直接复用现有登录态
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(createToken(user.getId(), user.getUsername()));
        return vo;
    }

    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {
        stringRedisTemplate.opsForValue().set(openidKey(ticket), openid, Duration.ofHours(1));

        // 扫码成功下发模板消息（失败不影响登录）
        try {
            String accessToken = getAccessToken();

            Map<String, Map<String, String>> data = new HashMap<>();
            WeixinTemplateMessageVO.put(data, WeixinTemplateMessageVO.TemplateKey.USER, openid);

            WeixinTemplateMessageVO templateMessageDTO = new WeixinTemplateMessageVO(openid, templateId);
            templateMessageDTO.setData(data);

            Call<Void> call = weixinApiService.sendMessage(accessToken, templateMessageDTO);
            call.execute();
        } catch (Exception ignored) {
            // 模板消息下发失败不阻断登录流程
        }
    }

    /**
     * 首次微信扫码：以 openid 自动注册平台用户
     */
    private SysUser autoRegister(String openid) {
        SysUser user = new SysUser();
        user.setUsername(openid);
        user.setPassword(""); // 微信用户无密码，仅可通过扫码登录
        user.setNickname("微信用户_" + openid.substring(Math.max(0, openid.length() - 6)));
        user.setUserType(JwtClaimsConstant.USER);
        user.setStatus(1);
        user.setOpenid(openid);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
        return sysUserMapper.getByOpenid(openid);
    }

    /**
     * 生成 JWT Token（与 AuthServiceImpl 保持一致）
     */
    private String createToken(Long id, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, id);
        claims.put(JwtClaimsConstant.USER_TYPE, JwtClaimsConstant.USER);
        claims.put(JwtClaimsConstant.USERNAME, username);
        return JwtUtil.createJwt(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);
    }

    private String getAccessToken() throws IOException {
        String key = accessTokenKey();
        String accessToken = stringRedisTemplate.opsForValue().get(key);
        if (accessToken != null && !accessToken.isBlank()) {
            return accessToken;
        }

        Call<WeixinTokenRes> call = weixinApiService.getToken("client_credential", appid, appSecret);
        WeixinTokenRes weixinTokenRes = call.execute().body();
        if (weixinTokenRes == null || weixinTokenRes.getAccess_token() == null) {
            throw new IOException("获取微信 access_token 失败");
        }
        accessToken = weixinTokenRes.getAccess_token();
        stringRedisTemplate.opsForValue().set(key, accessToken, Duration.ofSeconds(7000));
        return accessToken;
    }

    private String accessTokenKey() {
        return ACCESS_TOKEN_KEY_PREFIX + appid;
    }

    private String openidKey(String ticket) {
        return OPENID_KEY_PREFIX + ticket;
    }
}
