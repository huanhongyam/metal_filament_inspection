package com.kunpeng.metal_filament_inspection.controller;

import com.kunpeng.metal_filament_inspection.domain.dto.QuestionAskDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.vo.QuestionVO;
import com.kunpeng.metal_filament_inspection.service.IQuestionService;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI 智能问答")
@Slf4j
@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Autowired
    private IQuestionService questionService;

    @Operation(summary = "用户发起 AI 提问")
    @PostMapping("/ask")
    public Result<QuestionVO> ask(@Valid @RequestBody QuestionAskDTO dto) {
        Long userId = UserHolder.getUserId();
        log.info("用户 {} 发起提问 — deviceId: {}", userId, dto.getDeviceId());
        return questionService.askFromUser(userId, dto);
    }

    @Operation(summary = "分页查询问题记录")
    @GetMapping("/list")
    public Result<List<QuestionVO>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Integer responseStatus) {
        return Result.success(questionService.listPage(current, deviceId, responseStatus));
    }

    @Operation(summary = "查询单条问题详情")
    @GetMapping("/{id}")
    public Result<QuestionVO> detail(@PathVariable Long id) {
        QuestionVO vo = questionService.lambdaQuery()
                .eq(com.kunpeng.metal_filament_inspection.domain.entity.Question::getId, id)
                .oneOpt()
                .map(q -> cn.hutool.core.bean.BeanUtil.copyProperties(q, QuestionVO.class))
                .orElse(null);
        if (vo == null) {
            return Result.error(404, "问题记录不存在");
        }
        return Result.success(vo);
    }
}