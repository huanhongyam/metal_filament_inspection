package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.kunpeng.metal_filament_inspection.domain.dto.*;import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.mapper.WireMaterialMapper;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class WireMaterialServiceImpl extends ServiceImpl<WireMaterialMapper, WireMaterial> implements IWireMaterialService {
    @Autowired
    private IUserService userService;
    @Override
    public List<WireMaterialDTO> listQueryPage(Integer limit, WireMaterialQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
        // 字符串条件（精确匹配）
        if (StringUtils.hasText(queryDTO.getDeviceId())) {
            wrapper.eq(WireMaterial::getDeviceId, queryDTO.getDeviceId());
        }
        if (StringUtils.hasText(queryDTO.getManufacturer())) {
            wrapper.eq(WireMaterial::getManufacturer, queryDTO.getManufacturer());
        }
        if (StringUtils.hasText(queryDTO.getResponsiblePerson())) {
            wrapper.eq(WireMaterial::getResponsiblePerson, queryDTO.getResponsiblePerson());
        }
        if (StringUtils.hasText(queryDTO.getProcessType())) {
            wrapper.eq(WireMaterial::getProcessType, queryDTO.getProcessType());
        }
        if (StringUtils.hasText(queryDTO.getScenarioCode())) {
            wrapper.eq(WireMaterial::getScenarioCode, queryDTO.getScenarioCode());
        }
        // 数值等值条件
        if (queryDTO.getBatchNumber() != null) {
            wrapper.eq(WireMaterial::getBatchNumber, queryDTO.getBatchNumber());
        }
        if (queryDTO.getBatchNo() != null) {
            wrapper.eq(WireMaterial::getBatchNo, queryDTO.getBatchNo());
        }
        // 时间范围（例如查询创建时间前7天到指定时间）
        if (queryDTO.getCreateTime() != null) {
            LocalDateTime end = queryDTO.getCreateTime();
            LocalDateTime start = end.minusDays(7);
            wrapper.between(WireMaterial::getCreateTime, start, end);
        }
        // 2.限制条数（使用 last 拼接 LIMIT）
        if (limit > 1000) limit = 1000;
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        // 3. 执行查询
        List<WireMaterial> entities = baseMapper.selectList(wrapper);
        // 4. 转换为 DTO
        return entities.stream()
                .map(entity -> {
                    WireMaterialDTO dto = new WireMaterialDTO();
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public Result<Boolean> updateByBatchNumber(WireMaterialDTO wireMaterialDTO, Long batchNumber) {
        User user = userService.getById(UserHolder.getUserId());
        String userName = user.getUserName();
        log.info("管理员{}更新线材信息，批次号：{}", userName, batchNumber);
        UpdateWrapper<WireMaterial> updateWrapper = Wrappers.update();
        updateWrapper.eq("batch_number", batchNumber);
        WireMaterial wireMaterial = BeanUtil.copyProperties(wireMaterialDTO, WireMaterial.class);
        boolean isUpdate = update(wireMaterial, updateWrapper);
        return Result.success(isUpdate);
    }
    @Override
    public Result<Boolean> deleteById(Long batchNumber) {
        Long userId = UserHolder.getUserId();
        User user = userService.getById(userId);
        String userName = user.getUserName();
        log.info("管理员{}删除线材记录，批次号：{}", userName, batchNumber);
        return Result.success(removeById(batchNumber));
    }
    @Override
    public PageDTO listPage(Integer current) {
        PageHelper.startPage(current,SystemConstants.DEFAULT_PAGE_SIZE);
        List<WireMaterial> list = list();
        List<WireMaterialQueryDTO> page = list.stream().map(item -> {
            return BeanUtil.copyProperties(item, WireMaterialQueryDTO.class);
        }).toList();
        PageDTO pageDTO = new PageDTO<>();
        pageDTO.setCurrentPage(current);
        pageDTO.setRecords(page);
        pageDTO.setTotal((long) page.size());
        pageDTO.setPageSize(SystemConstants.DEFAULT_PAGE_SIZE);
        return pageDTO;
    }

    @Override
    public Result<Boolean> savewireMaterial(WireMaterialSaveDTO wireMaterialSaveDTO) {
        // 保存线材检测记录并返回结果
        WireMaterial wireMaterial = BeanUtil.copyProperties(wireMaterialSaveDTO, WireMaterial.class);
        boolean isSuccess = save(wireMaterial);
        if (isSuccess) {
            log.info("创建线材检测记录成功，设备ID：{}",
                    wireMaterial.getDeviceId());
            return Result.success();
        } else {
            log.error("创建线材检测记录成功，设备ID：{}",wireMaterial.getDeviceId());
            return Result.error("创建设备失败，请稍后重试");
        }
    }
}
