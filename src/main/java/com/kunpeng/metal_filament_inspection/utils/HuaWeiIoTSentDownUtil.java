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
     * 下发消息（使用系统标准格式）
     * 平台会封装成标准JSON：{"object_device_id":"...", "name":"...", "id":"...", "content":"..."}*
     * @param deviceId 设备ID
     * @param payload  消息内容（将自动转为JSON字符串放入content字段）
     */
    public void sendDownMessage(String deviceId, Object payload) {
        try {
            // 1. 构造符合格式的消息体[reference:4]
            Map<String, Object> standardMsg = new LinkedHashMap<>();
            standardMsg.put("name", "Surface_data");
            standardMsg.put("id", UUID.randomUUID().toString());
            // content字段必须是字符串，如果是对象则转为JSON
            String content = objectMapper.writeValueAsString(payload);
            standardMsg.put("content", content);
            // 2. 创建请求
            DeviceMessageRequest body = new DeviceMessageRequest()
                    .withMessage(standardMsg)          // 消息内容
                    .withPayloadFormat("standard");

            CreateMessageRequest request = new CreateMessageRequest()
                    .withDeviceId(deviceId)
                    .withBody(body);

            // 3. 执行下发
            CreateMessageResponse response = iotClient.createMessage(request);
            log.info("【系统格式】消息下发成功，设备: {}, 消息ID: {}", deviceId, response.getMessageId());

        } catch (ClientRequestException e) {
            log.error("【系统格式】消息下发失败，设备: {}, 错误码: {}, 错误信息: {}",
                    deviceId, e.getErrorCode(), e.getErrorMsg());
            throw new RuntimeException("消息下发失败: " + e.getErrorMsg(), e);
        } catch (Exception e) {
            log.error("【系统格式】消息下发异常，设备: {}", deviceId, e);
            throw new RuntimeException("消息下发异常: " + e.getMessage(), e);
        }
    }

}