package com.kunpeng.metal_filament_inspection;

import cn.hutool.core.util.RandomUtil;
import com.kunpeng.metal_filament_inspection.domain.dto.TaskDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.WireDTO;
import com.kunpeng.metal_filament_inspection.utils.HuaWeiIoTSentDownUtil;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.VerificationCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class SendDownTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private HuaWeiIoTSentDownUtil huaWeiIoTSentDownUtil;
    @Autowired
    private VerificationCodeUtil verificationCodeUtil;
//    @Test
//    public void testSend(){
////        TaskDTO taskDTO = new TaskDTO();
////        taskDTO.setBatchNumber(65987525348425749L);
//        WireDTO wireDTO = new WireDTO();
//        wireDTO.setBatchNo(25L);
//        wireDTO.setRollNo(4L);
//        rabbitTemplate.convertAndSend("senddown.exchange","senddown.task", wireDTO);
//    }
//    @Test
//    public void testHuaWei(){
//        huaWeiIoTSentDownUtil.sendDownMessage(SystemConstants.HUAWEI_DEVICE_ID,"batr");
//    }
//    @Test
//    public void testRandom(){
//        String code = RandomUtil.randomString(6);
//        log.info(code);
//    }
//    @Test
//    public void sendMailCode(){
//        String s = verificationCodeUtil.sendVerificationCode("shendie402170@126.com");
//        log.info(s);
//    }

//    @Test
//    public void testDelayQueue() throws Exception {
//        Long testMsg = 65951288709349395L;
//        String exchange = "delay.exchange";
//        String routingKey = "triggerEvaluation.task";
//        String targetQueue = "triggerEvaluation.queue";
//
//        // 1. 发送到延迟交换机
//        rabbitTemplate.convertAndSend(exchange, routingKey, testMsg);
//        log.info("📤 已发送延迟消息: {} → {} / routingKey={}", exchange, testMsg, routingKey);
//
//        // 2. 立即尝试接收 — 应该为 null（消息在延迟队列中）
//        Object immediate = rabbitTemplate.receiveAndConvert(targetQueue, 2000);
//        if (immediate == null) {
//            log.info("✅ 立即接收为 null，消息正在延迟队列中（预期行为）");
//        } else {
//            log.warn("❌ 意外收到消息: {}", immediate);
//        }
//
//        // 3. 等待 TTL(10s) + buffer(5s)
//        log.info("⏳ 等待 15 秒后接收...");
//        Thread.sleep(15_000);
//
//        // 4. 延迟后接收 — 应该能收到
//        Object delayed = rabbitTemplate.receiveAndConvert(targetQueue, 5000);
//        if (delayed != null) {
//            log.info("✅ 延迟后收到消息: {} (类型: {})", delayed, delayed.getClass().getSimpleName());
//        } else {
//            log.error("❌ 超时未收到消息，延迟队列可能未生效");
//        }
//    }

}
