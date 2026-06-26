package com.kunpeng.metal_filament_inspection.amqp.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialQueryDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialSaveDTO;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class IotAmqpListener {
    private final ObjectMapper objectMapper;
    private final IWireMaterialService wireMaterialService;
    private final IdWorker idWorker;

    public IotAmqpListener(ObjectMapper objectMapper, IWireMaterialService wireMaterialService,IdWorker idWorker) {
        this.objectMapper = objectMapper;
        this.wireMaterialService = wireMaterialService;
        this.idWorker = idWorker;
    }
    @JmsListener(
            destination = "${huawei.iot.amqp.queue-name}",
            containerFactory = "iotAmqpListenerFactory"
    )
    public void onMessage(Message message) {
        String payload = null;
        try {
            payload = extractPayload(message);
            JsonNode root = objectMapper.readTree(payload);
            String deviceId = root.at("/notify_data/header/device_id").asText();
            log.info("✅ 收到设备 [{}] 的属性上报", deviceId);
            // 直接定位到 services[0].properties."1"
            JsonNode dataNode = root.at("/notify_data/body/services/0/properties/1");
            if (!dataNode.isMissingNode()) {
                WireMaterialSaveDTO dto = objectMapper.convertValue(dataNode, WireMaterialSaveDTO.class);
                log.info("📦 解析结果: deviceId={}, diameter={}, batchNo={}, rollNo={}",
                        dto.getDeviceId(), dto.getDiameter(), dto.getBatchNo(), dto.getRollNo());
                dto.setBatchNumber(idWorker.generateId(SystemConstants.WIRE_MATERIAL_PREFIX));
                wireMaterialService.savewireMaterial(dto);
            } else {
                log.warn("未找到 properties.1 节点");
            }
            message.acknowledge();
        } catch (Exception e) {
            log.error("消息处理失败: {}", payload, e);
        }
    }
    private String extractPayload(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            return ((TextMessage) message).getText();
        } else if (message instanceof BytesMessage) {
            BytesMessage bytesMsg = (BytesMessage) message;
            byte[] buffer = new byte[(int) bytesMsg.getBodyLength()];
            bytesMsg.readBytes(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        }
        return message.toString();
    }
}