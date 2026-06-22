package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.domain.dto.ApplicationScenarioDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.entity.ApplicationScenario;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.mapper.ApplicationScenarioMapper;
import com.kunpeng.metal_filament_inspection.service.IApplicationScenarioService;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.BusinessException;
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
    public List<ApplicationScenarioDTO> getScenarioList(Integer current, String wireType, String scenarioName) {
        PageHelper.startPage(current, SystemConstants.DEFAULT_PAGE_SIZE);
        // 构建动态查询条件
        QueryWrapper<ApplicationScenario> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(wireType)) {
            queryWrapper.eq("wire_type", wireType);   // 精确匹配，字段名根据实际表结构调整
        }
        if (StringUtils.hasText(scenarioName)) {
            queryWrapper.like("scenario_name", scenarioName);
        }
        // 执行带条件的分页查询
        List<ApplicationScenario> list = applicationScenarioMapper.selectList(queryWrapper);
        List<ApplicationScenarioDTO> page = list.stream().map(item -> {
            return BeanUtil.copyProperties(item, ApplicationScenarioDTO.class);
        }).toList();
        return page;
    }
    @RequireAdmin
    @Override
    public Boolean createScenario(ApplicationScenarioDTO applicationScenarioDTO) {
        // 权限检查：仅管理员
        Long userId = UserHolder.getUserId();
        log.info("管理员用户{}创建应用场景，场景编号：{}，场景名称：{}", userId, applicationScenarioDTO.getScenarioCode(), applicationScenarioDTO.getScenarioName());
        ApplicationScenario applicationScenario = BeanUtil.copyProperties(applicationScenarioDTO, ApplicationScenario.class);
        return save(applicationScenario);
    }
}
