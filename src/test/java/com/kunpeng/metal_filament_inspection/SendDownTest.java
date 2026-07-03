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

}
