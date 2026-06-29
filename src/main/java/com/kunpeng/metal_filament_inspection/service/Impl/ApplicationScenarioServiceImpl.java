package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kunpeng.metal_filament_inspection.domain.dto.ApplicationScenarioDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.PageDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.ApplicationScenario;
import com.kunpeng.metal_filament_inspection.mapper.ApplicationScenarioMapper;
import com.kunpeng.metal_filament_inspection.service.IApplicationScenarioService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class ApplicationScenarioServiceImpl extends ServiceImpl<ApplicationScenarioMapper, ApplicationScenario> implements IApplicationScenarioService {

    @Override
    public PageDTO<ApplicationScenarioDTO> getScenarioList(Integer current, String wireType, String scenarioName) {
        Page<ApplicationScenario> page = new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE);
        // 构建动态查询条件
        LambdaQueryWrapper<ApplicationScenario> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(wireType)) {
            wrapper.eq(ApplicationScenario::getWireType, wireType);
        }
        if (StringUtils.hasText(scenarioName)) {
            wrapper.like(ApplicationScenario::getScenarioName, scenarioName);
        }
        // 按创建时间倒序等
        wrapper.orderByDesc(ApplicationScenario::getCreateTime);
        // 分页查询
        IPage<ApplicationScenario> pageResult = page(page, wrapper);
        // 转换数据
        List<ApplicationScenarioDTO> dtoList = pageResult.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, ApplicationScenarioDTO.class))
                .collect(Collectors.toList());
        PageDTO<ApplicationScenarioDTO> pageDTO = new PageDTO<>();
        pageDTO.setCurrentPage((int) pageResult.getCurrent());
        pageDTO.setPageSize((int) pageResult.getSize());
        pageDTO.setTotal(pageResult.getTotal());
        pageDTO.setRecords(dtoList);
        return pageDTO;
    }
    @Override
    public Boolean createScenario(ApplicationScenarioDTO applicationScenarioDTO) {
        // 权限检查：仅管理员
        Long userId = UserHolder.getUserId();
        log.info("管理员用户{}创建应用场景，场景编号：{}，场景名称：{}", userId, applicationScenarioDTO.getScenarioCode(), applicationScenarioDTO.getScenarioName());
        ApplicationScenario applicationScenario = BeanUtil.copyProperties(applicationScenarioDTO, ApplicationScenario.class);
        return save(applicationScenario);
    }
}
