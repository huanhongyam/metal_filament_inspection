package com.kunpeng.metal_filament_inspection.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
}
