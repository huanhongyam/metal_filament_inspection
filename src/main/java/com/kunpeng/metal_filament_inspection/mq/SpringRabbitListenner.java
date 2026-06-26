package com.kunpeng.metal_filament_inspection.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class SpringRabbitListenner {


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "direct.queue1"),
            exchange = @Exchange(name = "mq.inspection.data.raw",type = ExchangeTypes.DIRECT),
            key = {"red","blue"}
    ))
    public void ListendirectQueue1(String message) throws InterruptedException {
        log.info("消费者1收到了消息:【{}】{}",message, LocalDateTime.now());
    }
}
