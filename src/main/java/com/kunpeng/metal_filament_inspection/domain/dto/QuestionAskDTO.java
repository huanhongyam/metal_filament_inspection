package com.kunpeng.metal_filament_inspection.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "AI 问答请求")
public class QuestionAskDTO {

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "6a32861318855b39c5258e08_test")
    private String deviceId;

    @NotBlank(message = "提问内容不能为空")
    @Schema(description = "提问内容", example = "分析设备最近24小时的线材质量趋势")
    private String questionContent;
}