package com.connor.newsplatform.service.pay;

import com.connor.newsplatform.common.constant.OrderStatus;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.properties.PayProperties;
import com.connor.newsplatform.dao.NewsArticleMapper;
import com.connor.newsplatform.dao.PayOrderMapper;
import com.connor.newsplatform.domain.po.NewsArticle;
import com.connor.newsplatform.domain.po.PayOrder;
import com.connor.newsplatform.domain.req.CreatePayReq;
import com.connor.newsplatform.domain.res.PayOrderRes;
import com.connor.newsplatform.service.websocket.AuditWebSocketHandler;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 订单服务实现。状态机：CREATE → PAY_WAIT → PAY_SUCCESS / CLOSE。
 * mock 模式（media.pay.mock=true，默认）下 payUrl 是平台内 mock 支付跳转地址；
 * 置 false 走真实支付宝沙箱：payUrl 为支付宝电脑网站支付收银台表单（HTML），支付成功由 AliPayController 异步通知回调。
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final PayOrderMapper payOrderMapper;
    private final NewsArticleMapper articleMapper;
    private final AuditWebSocketHandler webSocketHandler;
    private final RabbitTemplate rabbitTemplate;
    private final PayProperties payProperties;
    private final AlipayClient alipayClient;
    private final String alipayNotifyUrl;
    private final String alipayReturnUrl;

    public OrderServiceImpl(PayOrderMapper payOrderMapper,
                            NewsArticleMapper articleMapper,
                            AuditWebSocketHandler webSocketHandler,
                            RabbitTemplate rabbitTemplate,
                            PayProperties payProperties,
                            AlipayClient alipayClient,
                            @Value("${alipay.notify_url}") String alipayNotifyUrl,
                            @Value("${alipay.return_url}") String alipayReturnUrl) {
        this.payOrderMapper = payOrderMapper;
        this.articleMapper = articleMapper;
        this.webSocketHandler = webSocketHandler;
        this.rabbitTemplate = rabbitTemplate;
        this.payProperties = payProperties;
        this.alipayClient = alipayClient;
        this.alipayNotifyUrl = alipayNotifyUrl;
        this.alipayReturnUrl = alipayReturnUrl;
    }

    @Override
    @Transactional
    public PayOrderRes createOrder(Long userId, CreatePayReq req) {
        NewsArticle article = articleMapper.getById(req.getArticleId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (article.getIsPaid() == null || article.getIsPaid() != 1) {
            throw new BusinessException("该文章不支持付费阅读");
        }
        if (Objects.equals(article.getAuthorId(), userId)) {
            throw new BusinessException("作者本人无需购买");
        }
        // 已购买直接返回已支付订单
        PayOrder paid = payOrderMapper.getPaidByUserArticle(userId, article.getId());
        if (paid != null) {
            return PayOrderRes.builder()
                    .orderId(paid.getOrderId())
                    .payUrl(null)
                    .totalAmount(paid.getTotalAmount())
                    .status(paid.getStatus())
                    .build();
        }
        // 复用最近一笔未关单订单（用户中途退出再回来直接用旧 payUrl）
        PayOrder latest = payOrderMapper.getLatestUnpaid(userId, article.getId());
        if (latest != null) {
            if (OrderStatus.PAY_WAIT.name().equals(latest.getStatus())) {
                return PayOrderRes.builder()
                        .orderId(latest.getOrderId())
                        .payUrl(latest.getPayUrl())
                        .totalAmount(latest.getTotalAmount())
                        .status(latest.getStatus())
                        .build();
            }
            // status=CREATE 需要补建预支付单
            return doPrepayOrder(latest);
        }
        // 新建订单
        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setArticleId(article.getId());
        order.setArticleTitle(article.getTitle());
        order.setOrderId(RandomStringUtils.randomNumeric(16));
        order.setOrderTime(LocalDateTime.now());
        order.setTotalAmount(article.getPrice());
        order.setStatus(OrderStatus.CREATE.name());
        payOrderMapper.insert(order);
        return doPrepayOrder(order);
    }

    /** 创建预支付单，生成 payUrl */
    private PayOrderRes doPrepayOrder(PayOrder order) {
        String payUrl;
        if (payProperties.isMock()) {
            // mock 模式：payUrl 指向平台内 mock 支付页，携带订单号
            payUrl = "/pay/mock?orderId=" + order.getOrderId();
        } else {
            // 真沙箱模式：调用支付宝 SDK 生成电脑网站支付收银台表单（HTML）
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayNotifyUrl);
            request.setReturnUrl(alipayReturnUrl);
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", order.getOrderId());
            bizContent.put("total_amount", order.getTotalAmount().toString());
            bizContent.put("subject", order.getArticleTitle());
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            request.setBizContent(bizContent.toString());
            try {
                payUrl = alipayClient.pageExecute(request).getBody();
            } catch (AlipayApiException e) {
                log.error("支付宝预支付单生成失败 orderId={}", order.getOrderId(), e);
                throw new BusinessException("支付宝预支付单生成失败：" + e.getMessage());
            }
        }
        order.setPayUrl(payUrl);
        order.setStatus(OrderStatus.PAY_WAIT.name());
        payOrderMapper.updatePayInfo(order);
        return PayOrderRes.builder()
                .orderId(order.getOrderId())
                .payUrl(payUrl)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }

    @Override
    @Transactional
    public void changeOrderPaySuccess(String orderId, String tradeNo) {
        PayOrder order = payOrderMapper.getByOrderId(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (OrderStatus.PAY_SUCCESS.name().equals(order.getStatus())) {
            return; // 幂等
        }
        LocalDateTime now = LocalDateTime.now();
        payOrderMapper.updatePaySuccess(orderId, now, tradeNo);

        // 通知作者 + 用户本人
        if (order.getUserId() != null) {
            String userMsg = "{\"type\":\"PAY_SUCCESS\",\"title\":\"支付成功\",\"content\":\"你购买的《"
                    + order.getArticleTitle() + "》已解锁完整正文\",\"time\":\"" + now + "\"}";
            webSocketHandler.sendToUser(order.getUserId(), userMsg);
        }

        JSONObject message = new JSONObject();
        message.put("orderId", orderId);
        message.put("userId", order.getUserId());
        message.put("articleId", order.getArticleId());
        message.put("articleTitle", order.getArticleTitle());
        message.put("payTime", now.toString());
        rabbitTemplate.convertAndSend(
                PayRabbitMQConstants.EXCHANGE_NAME,
                PayRabbitMQConstants.PAY_SUCCESS_ROUTING_KEY,
                message.toJSONString()
        );
    }

    @Override
    public List<String> queryNoPayNotifyOrderList() {
        return payOrderMapper.queryNoPayNotifyOrderList(10);
    }

    @Override
    public boolean isPurchased(Long userId, Long articleId) {
        return payOrderMapper.getPaidByUserArticle(userId, articleId) != null;
    }

    @Override
    public PayOrder getByOrderId(String orderId) {
        return payOrderMapper.getByOrderId(orderId);
    }

    @Override
    public List<String> queryTimeoutCloseList() {
        return payOrderMapper.queryTimeoutCloseList(payProperties.getTimeoutMinutes(), 50);
    }

    @Override
    @Transactional
    public boolean changeOrderClose(String orderId) {
        payOrderMapper.updateClose(orderId);
        return true;
    }
}
