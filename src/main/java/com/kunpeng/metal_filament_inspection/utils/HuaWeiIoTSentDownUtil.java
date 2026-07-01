package com.kunpeng.metal_filament_inspection.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaweicloud.sdk.core.exception.ClientRequestException;
import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import com.huaweicloud.sdk.iotda.v5.model.CreateMessageRequest;
import com.huaweicloud.sdk.iotda.v5.model.CreateMessageResponse;
import com.huaweicloud.sdk.iotda.v5.model.DeviceMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class HuaWeiIoTSentDownUtil {

    @Autowired
    private IoTDAClient iotClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 下发消息（topic = name 字段，设备端按 name 订阅区分消息类型）
     * @param deviceId 设备ID
     * @param topic    消息主题（对应华为 IoT 标准格式的 name 字段）
     * @param payload  消息内容（对象，自动序列化为 JSON 字符串）
     */
    public void sendDownMessage(String deviceId, String topic, Object payload) {
        try {
            Map<String, Object> standardMsg = new LinkedHashMap<>();
            standardMsg.put("name", topic);
            standardMsg.put("id", UUID.randomUUID().toString());
            standardMsg.put("content", objectMapper.writeValueAsString(payload));

            DeviceMessageRequest body = new DeviceMessageRequest()
                    .withMessage(standardMsg)
                    .withPayloadFormat("standard");

            CreateMessageRequest request = new CreateMessageRequest()
                    .withDeviceId(deviceId)
                    .withBody(body);

            CreateMessageResponse response = iotClient.createMessage(request);
            log.info("消息下发成功 — deviceId: {}, topic: {}, msgId: {}",
                    deviceId, topic, response.getMessageId());

        } catch (ClientRequestException e) {
            log.error("消息下发失败 — deviceId: {}, topic: {}, 错误码: {}, 错误信息: {}",
                    deviceId, topic, e.getErrorCode(), e.getErrorMsg());
            throw new RuntimeException("消息下发失败: " + e.getErrorMsg(), e);
        } catch (Exception e) {
            log.error("消息下发异常 — deviceId: {}, topic: {}", deviceId, topic, e);
            throw new RuntimeException("消息下发异常: " + e.getMessage(), e);
        }
    }
    /**
     * 下发消息（使用默认 topic = SystemConstants.DEFAULT_DOWNLINK_TOPIC）
     */
    public void sendDownMessage(String deviceId, Object payload) {
        sendDownMessage(deviceId, SystemConstants.DEFAULT_SENDDOWN_TOPIC, payload);
    }

}