package com.connor.newsplatform.web.controller.pay;

import com.alipay.api.internal.util.AlipaySignature;
import com.connor.newsplatform.service.pay.OrderService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/alipay")
public class AliPayController {

    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;

    @Resource
    private OrderService orderService;

    /**
     * 支付宝异步通知回调（内网穿透公网地址：http://39.102.79.179:28080/api/v1/alipay/alipay_notify_url）
     * 验签通过后更新订单支付状态
     */
    @PostMapping(value = "alipay_notify_url")
    public String payNotify(HttpServletRequest request) {
        log.info("支付宝支付回调，交易状态 {}", request.getParameter("trade_status"));

        if (!"TRADE_SUCCESS".equals(request.getParameter("trade_status"))) {
            return "false";
        }

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        String outTradeNo = params.get("out_trade_no"); // 商户订单号 = 我们的 orderId
        String tradeNo = params.get("trade_no");         // 支付宝交易号

        String sign = params.get("sign");
        boolean checkSignature;
        try {
            String content = AlipaySignature.getSignCheckContentV1(params);
            checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayPublicKey, "UTF-8");
        } catch (Exception e) {
            // 非法签名格式（如随机探测）会导致验签抛异常，按验签失败处理，避免返回系统异常
            log.error("支付宝回调验签异常（可能是非法签名格式），orderId={}", outTradeNo, e);
            return "false";
        }
        if (!checkSignature) {
            log.error("支付宝回调验签失败，orderId={}", outTradeNo);
            return "false";
        }

        log.info("支付宝回调验签通过，orderId={} alipayTradeNo={}", outTradeNo, tradeNo);
        orderService.changeOrderPaySuccess(outTradeNo, tradeNo);
        return "success";
    }
}
