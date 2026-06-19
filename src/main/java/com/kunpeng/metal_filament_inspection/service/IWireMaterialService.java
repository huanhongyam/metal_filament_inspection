package com.kunpeng.metal_filament_inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IWireMaterialService extends IService<WireMaterial> {
    Result<List<WireMaterialDTO>> listPage(Integer current);


    Result<Boolean> updateByBatchNumber(@Valid WireMaterialDTO wireMaterialDTO, @NotBlank(message = "批次号不能为空") String batchNumber);

    Result<Boolean> deleteById(@NotBlank(message = "批次号不能为空") String batchNumber);
}
