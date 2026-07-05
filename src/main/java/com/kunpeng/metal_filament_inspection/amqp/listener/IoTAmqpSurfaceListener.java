package com.kunpeng.metal_filament_inspection.amqp.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunpeng.metal_filament_inspection.domain.dto.WireDTO;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IoTAmqpSurfaceListener {

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public IoTAmqpSurfaceListener(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @JmsListener(
            destination = "${huawei.iot.amqp.queue-name-surface}",
            containerFactory = "iotAmqpListenerFactory"
    )
    public void onMessage(String message){
        String payload = null;
        try {
            payload = message;
            JsonNode root = objectMapper.readTree(payload);
            // 定位到 services[0].properties.surface_data
            JsonNode dataNode1 = root.at(SystemConstants.HUAWEI_IOT_MESSAGE_SURFACE_PREFIX);
            if (!dataNode1.isMissingNode()) {
                WireDTO wireDTO = objectMapper.convertValue(dataNode1, WireDTO.class);
                Long batchNo = wireDTO.getBatchNo();
                Long rollNo = wireDTO.getRollNo();
                rabbitTemplate.convertAndSend(SystemConstants.RABBITMQ_EXCHANGE_SENDDOWN_EXCHANGE,SystemConstants.RABBITMQ_EXCHANGE_SENDDOWN_TASK,wireDTO);
                log.info("📤 下发任务已发送至 senddown.task, batchNo={},rollNo={}", batchNo,rollNo);
            }


        } catch (Exception e) {
            log.error("[IOT]:消息解析失败{}",payload,e);
        }
    }
}
