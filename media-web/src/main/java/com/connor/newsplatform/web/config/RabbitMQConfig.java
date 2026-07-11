package com.connor.newsplatform.web.config;

import com.connor.newsplatform.service.pay.PayRabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange payExchange() {
        return new DirectExchange(PayRabbitMQConstants.EXCHANGE_NAME);
    }

    @Bean
    public Queue paySuccessQueue() {
        return new Queue(PayRabbitMQConstants.PAY_SUCCESS_QUEUE, true);
    }

    @Bean
    public Binding paySuccessBinding() {
        return BindingBuilder
                .bind(paySuccessQueue())
                .to(payExchange())
                .with(PayRabbitMQConstants.PAY_SUCCESS_ROUTING_KEY);
    }
}
