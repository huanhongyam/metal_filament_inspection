package com.kunpeng.metal_filament_inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kunpeng.metal_filament_inspection.domain.dto.ApplicationScenarioDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.ApplicationScenario;
import jakarta.validation.Valid;

import java.util.List;

public interface IApplicationScenarioService extends IService<ApplicationScenario> {

    List<ApplicationScenarioDTO> getScenarioList(Integer current, String wireType, String scenarioName);

     Boolean createScenario(@Valid ApplicationScenarioDTO applicationScenarioDTO);
}
