package com.kunpeng.metal_filament_inspection.mq.consumer;

import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialUpdateDTO;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentTriggerConsumer {

    @Autowired
    private IWireMaterialService wireMaterialService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SystemConstants.RABBITMQ_QUEUE_TRIGGER_EVALUATION),
            exchange = @Exchange(name = SystemConstants.RABBITMQ_EXCHANGE_TRIGGER_EVALUATION),
            key = SystemConstants.RABBITMQ_TASK_TRIGGER_EVALUATION
    ))
    public void triggerEvaluation(Long batchNumber){
        Result<WireMaterialUpdateDTO> wireMaterialUpdateDTOResult = wireMaterialService.triggerEvaluation(batchNumber);
        int code = wireMaterialUpdateDTOResult.getCode();
        if (code == 200){
            log.info("主动触发Agent检测完成");
        }else {
            log.error("检测任务失败");
        }
    }
}
