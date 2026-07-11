package com.connor.newsplatform.web.controller.pay;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.domain.po.PayOrder;
import com.connor.newsplatform.domain.req.CreatePayReq;
import com.connor.newsplatform.domain.res.PayOrderRes;
import com.connor.newsplatform.service.pay.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端付费相关接口。
 * 路由挂在 /user/pay/**，复用现有 JwtTokenInterceptor（/user/** 已覆盖）。
 *
 * 端点：
 *  - POST /user/pay/create           创建支付订单，返回 payUrl
 *  - POST /user/pay/mock-success     mock 模式下"我已支付"按钮触发
 *  - GET  /user/pay/order/{orderId}  查订单状态（mock 页面轮询用）
 *  - GET  /user/pay/purchased        判断当前用户是否已购买某文章（?articleId=）
 */
@Slf4j
@RestController
@RequestMapping("/user/pay")
public class UserPayController {

    private final OrderService orderService;

    public UserPayController(OrderService orderService) {
        this.orderService = orderService;
    }

    @LogRecord("创建支付订单")
    @PostMapping("/create")
    public Result<PayOrderRes> createOrder(@Valid @RequestBody CreatePayReq req) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderService.createOrder(userId, req));
    }

    @LogRecord("mock 支付成功")
    @PostMapping("/mock-success")
    public Result<Void> mockSuccess(@RequestParam String orderId) {
        orderService.changeOrderPaySuccess(orderId, "MOCK-" + orderId);
        return Result.success();
    }

    @GetMapping("/order/{orderId}")
    public Result<PayOrder> getOrder(@PathVariable String orderId) {
        return Result.success(orderService.getByOrderId(orderId));
    }

    @GetMapping("/purchased")
    public Result<Boolean> isPurchased(@RequestParam Long articleId) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderService.isPurchased(userId, articleId));
    }
}
