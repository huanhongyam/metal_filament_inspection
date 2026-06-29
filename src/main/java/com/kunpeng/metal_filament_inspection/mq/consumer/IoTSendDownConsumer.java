package com.kunpeng.metal_filament_inspection.mq.consumer;

import com.kunpeng.metal_filament_inspection.domain.dto.DetectionBatchSummaryDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.TaskDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.DetectionBatchDTO;
import com.kunpeng.metal_filament_inspection.service.Impl.DetectionBatchServiceImpl;
import com.kunpeng.metal_filament_inspection.utils.DetectionSummary;
import com.kunpeng.metal_filament_inspection.utils.HuaWeiIoTSentDownUtil;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class IoTSendDownConsumer {
    @Autowired
    private HuaWeiIoTSentDownUtil huaWeiIoTSentDownUtil;
    @Autowired
    private DetectionBatchServiceImpl detectionBatchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SystemConstants.RABBITMQ_EXCHANGE_SENDDOWN_QUEUE, durable = "true"),
            exchange = @Exchange(name = SystemConstants.RABBITMQ_EXCHANGE_SENDDOWN_EXCHANGE),
            key = SystemConstants.RABBITMQ_EXCHANGE_SENDDOWN_TASK
    ))
    public void sendDownMessage4IoT(TaskDTO task){
        Long batchNumber = task.getBatchNumber();
        List<DetectionBatchDTO> detectionBatchDTOS = detectionBatchService.listFlawData(batchNumber);
        List<DetectionBatchSummaryDTO> detectionBatchSummaryDTOS = DetectionSummary.summarizeByBatchLoop(detectionBatchDTOS);
        huaWeiIoTSentDownUtil.sendDownMessage(SystemConstants.HUAWEI_DEVICE_ID,detectionBatchSummaryDTOS);
    }
}
