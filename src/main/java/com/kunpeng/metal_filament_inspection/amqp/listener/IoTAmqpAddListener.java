package com.kunpeng.metal_filament_inspection.amqp.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunpeng.metal_filament_inspection.domain.dto.TaskDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialSaveDTO;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IoTAmqpAddListener {
    private final ObjectMapper objectMapper;
    private final IWireMaterialService wireMaterialService;
    private final IdWorker idWorker;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    public IoTAmqpAddListener(ObjectMapper objectMapper, IWireMaterialService wireMaterialService, IdWorker idWorker) {
        this.objectMapper = objectMapper;
        this.wireMaterialService = wireMaterialService;
        this.idWorker = idWorker;
    }
    @JmsListener(
            destination = "${huawei.iot.amqp.queue-name}",
            containerFactory = "iotAmqpListenerFactory"
    )
    public void onMessage(String message){
        String payload = null;
        try {
            payload = message;
            JsonNode root = objectMapper.readTree(payload);
            // 定位到 services[0].properties."1"
            JsonNode dataNode1 = root.at(SystemConstants.HUAWEI_IOT_MESSAGE_PREFIX1);
            Long batchNumber = idWorker.generateId(SystemConstants.WIRE_MATERIAL_PREFIX);
            if (!dataNode1.isMissingNode()) {
                WireMaterialSaveDTO dto = objectMapper.convertValue(dataNode1, WireMaterialSaveDTO.class);
                log.info("✅ 收到设备 [{}] 的属性上报", dto.getDeviceId());
                log.info("📦 解析结果: deviceId={}, diameter={}, batchNo={}, rollNo={}",
                        dto.getDeviceId(), dto.getDiameter(), dto.getBatchNo(), dto.getRollNo());
                dto.setBatchNumber(batchNumber);
                Boolean isCheck = wireMaterialService.checkByBatchNoWithRollNo(dto.getBatchNo(), dto.getRollNo()).getData();
                if (Boolean.TRUE.equals(isCheck)){
                    log.info("⏭️ 线材记录已存在，跳过");
                    return;
                }
                wireMaterialService.saveWireMaterial(dto);
                // 异步发送检测任务
                // 定位到 services[0].properties."2"
                JsonNode dataNode2 = root.at(SystemConstants.HUAWEI_IOT_MESSAGE_PREFIX2);
                if (dataNode2.isMissingNode()) {
                    log.info("未找到 properties.2，不触发检测任务");
                    return;
                }
                    TaskDTO taskDTO = objectMapper.convertValue(dataNode2, TaskDTO.class);
                    taskDTO.setBatchNumber(batchNumber);
                    String exchange = SystemConstants.RABBITMQ_EXCHANGE_DETECT_TASK;
                    rabbitTemplate.convertAndSend(exchange, "detect.task",taskDTO);
                    log.info("📤 检测任务已发送至 detect.task, batchNumber={}", batchNumber);
            } else {
                log.warn("未找到 properties.1 节点");
            }
        } catch (Exception e) {
            log.error("[IOT]:消息解析失败{}",payload,e);
        }
    }
}