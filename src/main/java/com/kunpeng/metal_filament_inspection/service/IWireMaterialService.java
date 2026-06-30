package com.kunpeng.metal_filament_inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kunpeng.metal_filament_inspection.domain.dto.*;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface IWireMaterialService extends IService<WireMaterial> {
    List<WireMaterialDTO> listQueryPage( Integer limit, WireMaterialQueryDTO wireMaterialDTO);

    Result<Boolean> updateByBatchNumber(@Valid WireMaterialDTO wireMaterialDTO, @NotBlank(message = "批次号不能为空") Long batchNumber);

    Result<Boolean> deleteById(@NotBlank(message = "批次号不能为空") Long batchNumber);

    PageDTO listPage(Integer current);

    Result<Boolean> saveWireMaterial(WireMaterialSaveDTO wireMaterialSaveDTO);

    Result<Boolean> checkByBatchNoWithRollNo(Long batchNo, Long rollNo);

    List<WireMaterialDTO> listUnevaluated(Integer hours, Integer limit);

    Result<Boolean> updateEvaluation(Long batchNumber, @Valid WireMaterialUpdateDTO dto);

    Result<Integer> updateEvaluationBatch(@Valid List<WireMaterialUpdateDTO> dtoList);
}
