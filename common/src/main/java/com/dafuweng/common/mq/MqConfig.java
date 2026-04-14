package com.dafuweng.common.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    public static final String EXCHANGE_SALES = "sales.exchange";
    public static final String QUEUE_CONTRACT_SIGNED = "contract.signed.queue";
    public static final String ROUTING_CONTRACT_SIGNED = "contract.signed";

    public static final String QUEUE_LOAN_APPROVED = "loan.approved.queue";
    public static final String ROUTING_LOAN_APPROVED = "loan.approved";

    @Bean
    public DirectExchange salesExchange() {
        return new DirectExchange(EXCHANGE_SALES);
    }

    @Bean
    public Queue contractSignedQueue() {
        return QueueBuilder.durable(QUEUE_CONTRACT_SIGNED).build();
    }

    @Bean
    public Binding contractSignedBinding() {
        return BindingBuilder.bind(contractSignedQueue())
                .to(salesExchange())
                .with(ROUTING_CONTRACT_SIGNED);
    }

    @Bean
    public Queue loanApprovedQueue() {
        return QueueBuilder.durable(QUEUE_LOAN_APPROVED).build();
    }

    @Bean
    public Binding loanApprovedBinding() {
        return BindingBuilder.bind(loanApprovedQueue())
                .to(salesExchange())
                .with(ROUTING_LOAN_APPROVED);
    }
}
