package com.kunpeng.metal_filament_inspection.mq.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.kunpeng.metal_filament_inspection.domain.dto.*;
import com.kunpeng.metal_filament_inspection.service.IDetectionBatchService;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.service.Impl.DetectionBatchServiceImpl;
import com.kunpeng.metal_filament_inspection.utils.DetectionSummary;
import com.kunpeng.metal_filament_inspection.utils.HuaWeiIoTSentDownUtil;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class IoTSendDownConsumer {
    @Autowired
    private HuaWeiIoTSentDownUtil huaWeiIoTSentDownUtil;
    @Autowired
    private IDetectionBatchService detectionBatchService;
    @Autowired
    private IWireMaterialService wireMaterialService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SystemConstants.RABBITMQ_EXCHANGE_SENDDOWN_QUEUE, durable = "true"),
            exchange = @Exchange(name = SystemConstants.RABBITMQ_EXCHANGE_SENDDOWN_EXCHANGE),
            key = SystemConstants.RABBITMQ_EXCHANGE_SENDDOWN_TASK
    ))
    public void sendDownMessage4IoT(WireDTO task){
        Long batchNo = task.getBatchNo();
        Long rollNo = task.getRollNo();
        Long batchNumber = wireMaterialService.queryByBatchNoWithRollNo(batchNo, rollNo);
        // 查询相应检测记录并调用聚合逻辑聚合消息下发华为云
        List<DetectionBatchDTO> detectionBatchDTOS = detectionBatchService.listFlawData(batchNumber);
        DetectionBatchSummaryDTO detectionBatchSummaryDTOS = DetectionSummary.summarizeByBatchLoop(detectionBatchDTOS);
        DetectionBatchSummaryForIoTDTO detectionBatchSummaryForIoTDTO = BeanUtil.copyProperties(detectionBatchSummaryDTOS, DetectionBatchSummaryForIoTDTO.class);
        detectionBatchSummaryForIoTDTO.setModelEvaluationResult(wireMaterialService.getById(batchNumber).getModelEvaluationResult().getDescription());
        // 下发消息
        huaWeiIoTSentDownUtil.sendDownMessage(SystemConstants.HUAWEI_DEVICE_ID,SystemConstants.HUAWEI_SENDDOWN_SURFACE_DATA_TOPIC,detectionBatchSummaryForIoTDTO);
    }
}
