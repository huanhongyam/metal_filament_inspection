package com.kunpeng.metal_filament_inspection.amqp.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunpeng.metal_filament_inspection.service.IQuestionService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IoTAmqpQuestionListener {

    private final ObjectMapper objectMapper;
    private final IQuestionService questionService;

    public IoTAmqpQuestionListener(ObjectMapper objectMapper, IQuestionService questionService) {
        this.objectMapper = objectMapper;
        this.questionService = questionService;
    }

    @JmsListener(
            destination = "${huawei.iot.amqp.queue-name}",
            containerFactory = "iotAmqpListenerFactory"
    )
    public void onMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String questionPrefix = SystemConstants.HUAWEI_IOT_MESSAGE_AGENT_PREFIX;
            JsonNode questionNode = root.at(questionPrefix);

            if (questionNode.isMissingNode()) {
                return;
            }

            String deviceId = questionNode.has("device_id")
                    ? questionNode.get("device_id").asText()
                    : null;
            String questionContent = questionNode.has("question_content")
                    ? questionNode.get("question_content").asText()
                    : null;

            if (deviceId == null || questionContent == null) {
                log.warn("硬件问题上报字段缺失 — deviceId: {}, content: {}", deviceId, questionContent);
                return;
            }

            log.info("收到设备 {} 的提问: {}", deviceId, questionContent);
            questionService.askFromDevice(deviceId, questionContent);

        } catch (Exception e) {
            log.error("硬件问题消息解析失败", e);
        }
    }
}