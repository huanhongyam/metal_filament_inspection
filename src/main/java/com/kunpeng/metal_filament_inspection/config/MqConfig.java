package com.kunpeng.metal_filament_inspection.config;

import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqConfig {
    private final RabbitTemplate rabbitTemplate;

    @PostConstruct
    private void init(){
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                log.debug("监听到消息return callback");
                log.debug("交换机:{}",returnedMessage.getExchange());
                log.debug("routingKey:{}",returnedMessage.getRoutingKey());
                log.debug("message:{}",returnedMessage.getMessage());
                log.debug("replyCode:{}",returnedMessage.getReplyCode());
                log.debug("replayText:{}",returnedMessage.getReplyText());
            }
        });
    }
    // 正常交换机 & 队列（消费者监听此队列）

    @Bean
    public DirectExchange triggerEvaluationExchange(){
        return ExchangeBuilder.directExchange(SystemConstants.RABBITMQ_EXCHANGE_TRIGGER_EVALUATION)
                .build();
    }

    @Bean
    public Queue triggerEvaluationQueue(){
        return QueueBuilder.durable(SystemConstants.RABBITMQ_QUEUE_TRIGGER_EVALUATION).build();
    }

    @Bean
    public Binding triggerEvaluationBinding(Queue triggerEvaluationQueue, DirectExchange triggerEvaluationExchange){
        return BindingBuilder.bind(triggerEvaluationQueue).to(triggerEvaluationExchange).with(SystemConstants.RABBITMQ_TASK_TRIGGER_EVALUATION);
    }

    // 死信队列 — 60s TTL 后自动投递至 normal.exchange
    @Bean
    public DirectExchange delayExchange(){
        return ExchangeBuilder.directExchange("delay.exchange")
                .build();
    }
    @Bean
    public Queue delayQueue(){
        return QueueBuilder.durable("delay.queue")
                .deadLetterExchange(SystemConstants.RABBITMQ_EXCHANGE_TRIGGER_EVALUATION)
                .deadLetterRoutingKey(SystemConstants.RABBITMQ_TASK_TRIGGER_EVALUATION)
                .ttl(10_000)
                .build();
    }
    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange delayExchange){
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(SystemConstants.RABBITMQ_TASK_TRIGGER_EVALUATION);
    }
}
