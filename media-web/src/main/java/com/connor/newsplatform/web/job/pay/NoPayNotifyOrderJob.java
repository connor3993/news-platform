package com.connor.newsplatform.web.job.pay;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.connor.newsplatform.common.properties.PayProperties;
import com.connor.newsplatform.service.pay.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 主动查询可能漏掉支付宝异步回调的订单。
 * 参考 s-pay-mall-moni 的 NoPayNotifyOrderJob；mock 支付模式下不执行。
 */
@Slf4j
@Component
public class NoPayNotifyOrderJob {

    private final OrderService orderService;
    private final AlipayClient alipayClient;
    private final PayProperties payProperties;

    public NoPayNotifyOrderJob(OrderService orderService,
                               AlipayClient alipayClient,
                               PayProperties payProperties) {
        this.orderService = orderService;
        this.alipayClient = alipayClient;
        this.payProperties = payProperties;
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void exec() {
        if (payProperties.isMock()) {
            return;
        }

        try {
            List<String> orderIds = orderService.queryNoPayNotifyOrderList();
            if (orderIds == null || orderIds.isEmpty()) {
                return;
            }

            for (String orderId : orderIds) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(orderId);
                request.setBizModel(bizModel);

                AlipayTradeQueryResponse response = alipayClient.execute(request);
                String tradeStatus = response.getTradeStatus();
                if ("10000".equals(response.getCode())
                        && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))) {
                    orderService.changeOrderPaySuccess(orderId, response.getTradeNo());
                    log.info("漏回调订单已补偿为支付成功 orderId={} tradeNo={}", orderId, response.getTradeNo());
                }
            }
        } catch (Exception e) {
            log.error("检测未接收到或未正确处理的支付回调通知失败", e);
        }
    }
}
