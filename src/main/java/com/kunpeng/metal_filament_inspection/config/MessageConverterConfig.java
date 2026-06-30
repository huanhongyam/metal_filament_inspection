package com.kunpeng.metal_filament_inspection.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConverterConfig {
    @Bean
    public MessageConverter messageConverter(){
        // 1. 注册 Java 8 时间模块
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // 2. 传入自定义 ObjectMapper -> 符合LocalDateTime
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
