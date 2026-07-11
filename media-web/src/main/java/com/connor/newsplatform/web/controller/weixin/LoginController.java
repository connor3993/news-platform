package com.connor.newsplatform.web.controller.weixin;

import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.domain.vo.LoginVO;
import com.connor.newsplatform.service.ILoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 微信扫码登录 - 浏览器侧接口
 * 流程：前端拿 ticket -> 用 ticket 渲染二维码 -> 轮询 check_login 获取登录凭证
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/login")
public class LoginController {

    @Resource
    private ILoginService loginService;

    /**
     * 获取微信扫码登录二维码 ticket
     */
    @GetMapping("/weixin_qrcode_ticket")
    public Result<String> weixinQrCodeTicket() {
        try {
            String qrCodeTicket = loginService.createQrCodeTicket();
            log.info("生成微信扫码登录 ticket:{}", qrCodeTicket);
            return Result.success(qrCodeTicket);
        } catch (Exception e) {
            log.error("生成微信扫码登录 ticket 失败", e);
            return Result.error("生成微信扫码登录二维码失败");
        }
    }

    /**
     * 轮询登录态：已扫码返回 LoginVO（含 JWT），未扫码返回 null（前端继续轮询）
     */
    @GetMapping("/check_login")
    public Result<LoginVO> checkLogin(@RequestParam String ticket) {
        try {
            LoginVO loginVO = loginService.checkLogin(ticket);
            // loginVO 为 null 表示尚未扫码，前端继续轮询，不弹错误
            return Result.success(loginVO);
        } catch (Exception e) {
            log.error("扫码检测登录结果失败 ticket:{}", ticket, e);
            return Result.success(null);
        }
    }

}
