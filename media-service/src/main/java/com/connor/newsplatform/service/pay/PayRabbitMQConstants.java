package com.connor.newsplatform.service.pay;

public final class PayRabbitMQConstants {

    public static final String EXCHANGE_NAME = "media.news.pay.exchange";
    public static final String PAY_SUCCESS_QUEUE = "media.news.pay.success.queue";
    public static final String PAY_SUCCESS_ROUTING_KEY = "pay.success";

    private PayRabbitMQConstants() {
    }
}
