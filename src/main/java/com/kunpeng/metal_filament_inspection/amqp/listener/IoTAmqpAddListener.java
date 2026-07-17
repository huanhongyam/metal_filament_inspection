package com.kunpeng.metal_filament_inspection.amqp.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunpeng.metal_filament_inspection.domain.dto.TaskDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialSaveDTO;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.CacheKeyConstant;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class IoTAmqpAddListener {
    private final ObjectMapper objectMapper;
    private final IWireMaterialService wireMaterialService;
    private final IdWorker idWorker;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    public IoTAmqpAddListener(ObjectMapper objectMapper, IWireMaterialService wireMaterialService, IdWorker idWorker) {
        this.objectMapper = objectMapper;
        this.wireMaterialService = wireMaterialService;
        this.idWorker = idWorker;
    }
    @Transactional
    @JmsListener(
            destination = "${huawei.iot.amqp.queue-name-add}",
            containerFactory = "iotAmqpListenerFactory"
    )
    public void onMessage(String message){
        String payload = null;
        try {
            payload = message;
            JsonNode root = objectMapper.readTree(payload);
            // 定位到 services[0].properties.wire
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
                stringRedisTemplate.delete(CacheKeyConstant.CACHE_LIST_WITH_BATCH_AVG+1);
                stringRedisTemplate.delete(CacheKeyConstant.CACHE_LIST_WITH_BATCH_AVG+2);
                // 异步发送检测任务
                // 定位到 services[0].properties.wire.2
                JsonNode dataNode2 = dataNode1.at(SystemConstants.HUAWEI_IOT_MESSAGE_PREFIX2);
                if (dataNode2.isMissingNode()) {
                    log.info("未找到任务时间节点，不触发检测任务");
                    return;
                }
                    TaskDTO taskDTO = objectMapper.convertValue(dataNode2, TaskDTO.class);
                    taskDTO.setBatchNumber(batchNumber);
                    String exchange = SystemConstants.RABBITMQ_EXCHANGE_DETECT_TASK;
                    rabbitTemplate.convertAndSend(exchange, "detect.task",taskDTO);
                    rabbitTemplate.convertAndSend(
                            "delay.exchange",
                            SystemConstants.RABBITMQ_TASK_TRIGGER_EVALUATION,batchNumber);
                    log.info("📤 检测任务已发送至 detect.task, batchNumber={}", batchNumber);
            } else {
                log.warn("未找到 wire 属性节点");
            }
        } catch (Exception e) {
            log.error("[IOT]:消息解析失败{}",payload,e);
        }
    }
}
