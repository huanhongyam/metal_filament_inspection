package com.kunpeng.metal_filament_inspection.amqp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeviceMessageHandler {

    public void process(String payload) {
        // 在这里解析 JSON、入库、触发告警等
        log.info("处理设备消息: {}", payload);
    }
}