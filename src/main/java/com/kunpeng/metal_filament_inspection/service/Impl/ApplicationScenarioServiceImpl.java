package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.domain.dto.ApplicationScenarioDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.PageDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.ApplicationScenario;
import com.kunpeng.metal_filament_inspection.mapper.ApplicationScenarioMapper;
import com.kunpeng.metal_filament_inspection.service.IApplicationScenarioService;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Slf4j
@Transactional
@Service
public class ApplicationScenarioServiceImpl extends ServiceImpl<ApplicationScenarioMapper, ApplicationScenario> implements IApplicationScenarioService {
    @Resource
    private ApplicationScenarioMapper applicationScenarioMapper;
    @Resource
    private IUserService userService;

    @Override
    public PageDTO getScenarioList(Integer current, String wireType, String scenarioName) {
        PageHelper.startPage(current, SystemConstants.DEFAULT_PAGE_SIZE);
        QueryWrapper<ApplicationScenario> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(wireType)) {
            queryWrapper.eq("wire_type", wireType);
        }
        if (StringUtils.hasText(scenarioName)) {
            queryWrapper.like("scenario_name", scenarioName);
        }
        List<ApplicationScenario> list = applicationScenarioMapper.selectList(queryWrapper);
        Page<ApplicationScenario> page = (Page<ApplicationScenario>) list;
        long total = page.getTotal();
        List<ApplicationScenarioDTO> records = list.stream()
                .map(item -> BeanUtil.copyProperties(item, ApplicationScenarioDTO.class))
                .toList();
        PageDTO pageDTO = new PageDTO<>();
        pageDTO.setRecords(records);
        pageDTO.setPageSize(SystemConstants.DEFAULT_PAGE_SIZE);
        pageDTO.setTotal(total);
        pageDTO.setCurrentPage(current);
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
