package com.connor.newsplatform.service.pay;

import com.connor.newsplatform.domain.po.PayOrder;
import com.connor.newsplatform.domain.req.CreatePayReq;
import com.connor.newsplatform.domain.res.PayOrderRes;

import java.util.List;

/**
 * 付费订单服务。
 * 借鉴 s-pay-mall-moni 的 IOrderService 语义，简化掉 RPC/沙箱/Retrofit 等冗余。
 */
public interface OrderService {

    /** 创建支付订单（用户必须已登录） */
    PayOrderRes createOrder(Long userId, CreatePayReq req);

    /** 支付成功（来自回调 / mock） */
    void changeOrderPaySuccess(String orderId, String tradeNo);

    /** 扫描可能漏掉支付回调的等待支付订单 */
    List<String> queryNoPayNotifyOrderList();

    /** 当前用户对某文章是否已购买（已支付订单存在） */
    boolean isPurchased(Long userId, Long articleId);

    /** 根据订单号查 */
    PayOrder getByOrderId(String orderId);

    /** 扫描超时关单 */
    List<String> queryTimeoutCloseList();

    /** 关闭订单 */
    boolean changeOrderClose(String orderId);
}
