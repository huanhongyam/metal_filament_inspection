package com.kunpeng.metal_filament_inspection.controller;

import cn.hutool.core.bean.BeanUtil;
import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.domain.dto.ApplicationScenarioDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.PageDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.entity.ApplicationScenario;
import com.kunpeng.metal_filament_inspection.service.IApplicationScenarioService;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 应用场景管理控制器
 */
@Tag(name = "应用场景管理控制器接口")
@Slf4j
@RestController
@RequestMapping("/api/scenario")
public class ApplicationScenarioController {
    @Autowired
    private IApplicationScenarioService applicationScenarioService;
    @Autowired
    private IUserService userService;
    
    /**
     * 分页查询应用场景列表
     * 权限：已认证用户
     */
    @Operation(summary = "分页查询应用场景列表")
    @GetMapping("/list")
    public Result<PageDTO> getScenarioList(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "wireType", required = false) String wireType,
            @RequestParam(value = "scenarioName",required = false) String scenarioName
    ) {
        Long userId = UserHolder.getUserId();
        log.info("用户{}查询应用场景列表，页码：{}，每页大小：{}，线材类型筛选：{}，名称关键词：{}",
                userId, current, SystemConstants.DEFAULT_PAGE_SIZE, wireType, scenarioName);
        return Result.success(applicationScenarioService.getScenarioList(current,wireType,scenarioName));
    }
    /**
     * 根据应用场景编号查询应用场景信息
     * 权限：已认证用户
     */
    @Operation(summary = "根据应用场景编号查询应用场景信息")
    @GetMapping("/{scenarioCode}")
    public Result<ApplicationScenarioDTO> getScenarioByCode(
            @PathVariable  Long scenarioCode) {

        Long userId = UserHolder.getUserId();
        log.info("用户{}查询应用场景信息，场景编号：{}", userId, scenarioCode);
        ApplicationScenarioDTO applicationScenarioDTO = BeanUtil.copyProperties(applicationScenarioService.getById(scenarioCode), ApplicationScenarioDTO.class);
        return Result.success(applicationScenarioDTO);
    }
    /**
     * 创建应用场景
     * 权限：仅管理员用户（roleId=1）
     */
    @RequireAdmin
    @Operation(summary = "创建应用场景")
    @PostMapping
    public Result<Boolean> createScenario(
            @Valid @RequestBody ApplicationScenarioDTO applicationScenarioDTO) {
        return Result.success(applicationScenarioService.createScenario(applicationScenarioDTO));
    }
    /**
     * 查询应用场景总数
     * 权限：已认证用户
     */
    @Operation(summary = "查询应用场景总数")
    @GetMapping
    public Result<Long> countScenario() {
        return Result.success(applicationScenarioService.count());
    }

    /**
     * 更新应用场景
     * 权限：仅管理员用户（roleId=1）
     */
    @RequireAdmin
    @Operation(summary = "更新应用场景")
    @PutMapping("/{scenarioCode}")
    public Result<Boolean> updateScenario(
            @Valid @RequestBody ApplicationScenarioDTO applicationScenarioDTO) {
        Long userId = UserHolder.getUserId();
        log.info("管理员用户{}更新应用场景，场景编号：{}，场景名称：{}", userId, applicationScenarioDTO.getScenarioCode(),applicationScenarioDTO.getScenarioName());
        ApplicationScenario applicationScenario = BeanUtil.copyProperties(applicationScenarioDTO, ApplicationScenario.class);
        return Result.success(applicationScenarioService.updateById(applicationScenario));
    }
    /**
     * 删除应用场景
     * 权限：仅管理员用户（roleId=1）
     */
    @RequireAdmin
    @Operation(summary = "删除应用场景")
    @DeleteMapping("/{scenarioCode}")
    public Result<Boolean> deleteScenario(
            @PathVariable @Pattern(regexp = "^\\d{2}$", message = "应用场景编号必须是数字") String scenarioCode) {
        Long userId = UserHolder.getUserId();

        log.info("管理员用户{}删除应用场景，场景编号：{}", userId, scenarioCode);

        return Result.success(applicationScenarioService.removeById(scenarioCode));
    }

} 