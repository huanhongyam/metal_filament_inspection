package com.kunpeng.metal_filament_inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kunpeng.metal_filament_inspection.domain.dto.QuestionAskDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.entity.Question;
import com.kunpeng.metal_filament_inspection.domain.vo.QuestionVO;

import java.util.List;

public interface IQuestionService extends IService<Question> {

    /**
     * 用户从后端发起提问（需要 userId）
     */
    Result<QuestionVO> askFromUser(Long userId, QuestionAskDTO dto);

    /**
     * 硬件通过 JMS 发起提问（无 userId，仅 deviceId）
     */
    Result<QuestionVO> askFromDevice(String deviceId, String questionContent);

    /**
     * 分页查询问题列表
     */
    List<QuestionVO> listPage(Integer current, String deviceId, Integer responseStatus);
}