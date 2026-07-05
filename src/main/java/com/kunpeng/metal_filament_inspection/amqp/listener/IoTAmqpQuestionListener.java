package com.kunpeng.metal_filament_inspection.amqp.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.vo.QuestionVO;
import com.kunpeng.metal_filament_inspection.service.IQuestionService;
import com.kunpeng.metal_filament_inspection.utils.HuaWeiIoTSentDownUtil;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IoTAmqpQuestionListener {

    private final ObjectMapper objectMapper;
    private final IQuestionService questionService;
    private final HuaWeiIoTSentDownUtil huaWeiIoTSentDownUtil;

    public IoTAmqpQuestionListener(ObjectMapper objectMapper, IQuestionService questionService, HuaWeiIoTSentDownUtil huaWeiIoTSentDownUtil) {
        this.objectMapper = objectMapper;
        this.questionService = questionService;
        this.huaWeiIoTSentDownUtil = huaWeiIoTSentDownUtil;
    }

    @JmsListener(
            destination = "${huawei.iot.amqp.queue-name-question}",
            containerFactory = "iotAmqpListenerFactory"
    )
    public void onMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode questionNode = root.at(SystemConstants.HUAWEI_IOT_MESSAGE_AGENT_PREFIX);

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
            Result<QuestionVO> questionVOResult = questionService.askFromDevice(deviceId, questionContent);
            String aiResponseContent = questionVOResult.getData().getAiResponseContent();
            huaWeiIoTSentDownUtil.sendDownMessage(SystemConstants.HUAWEI_DEVICE_ID,SystemConstants.HUAWEI_SENDDOWN_QUESTION_TOPIC,aiResponseContent);

        } catch (Exception e) {
            log.error("硬件问题消息解析失败", e);
        }
    }
}
