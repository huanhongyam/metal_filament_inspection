package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
        // 精确匹配
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
        // 查询创建时间前7天到指定时间
        if (queryDTO.getCreateTime() != null) {
            LocalDateTime end = queryDTO.getCreateTime();
            LocalDateTime start = end.minusDays(7);
            wrapper.between(WireMaterial::getCreateTime, start, end);
        }
        // 限制条数（使用 last 拼接 LIMIT）
        if (limit > 1000) limit = 1000;
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        List<WireMaterial> entities = baseMapper.selectList(wrapper);
        // 转换为 DTO
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
    public PageDTO<WireMaterialQueryDTO> listPage(Integer current) {
        Page<WireMaterial> page = new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE);
        // 执行分页查询
        IPage<WireMaterial> pageResult = page(page, null);
        // 转换当前页数据为 DTO
        List<WireMaterialQueryDTO> dtoList = pageResult.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, WireMaterialQueryDTO.class))
                .collect(Collectors.toList());
        PageDTO<WireMaterialQueryDTO> pageDTO = new PageDTO<>();
        pageDTO.setCurrentPage((int) pageResult.getCurrent());
        pageDTO.setPageSize((int) pageResult.getSize());
        pageDTO.setTotal(pageResult.getTotal());
        pageDTO.setRecords(dtoList);
        return pageDTO;
    }

    @Override
    public Result<Boolean> saveWireMaterial(WireMaterialSaveDTO wireMaterialSaveDTO) {
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

    @Override
    public Result<Boolean> checkByBatchNoWithRollNo(Long batchNo, Long rollNo) {
        boolean exists = query().eq("batch_no", batchNo)
                .eq("roll_no", rollNo)
                .exists();
        return Result.success(exists);
    }
    @Override
    public List<WireMaterialDTO> listUnevaluated(Integer hours, Integer limit) {
        if (hours == null || hours <= 0) {
            hours = 24;
        }
        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WireMaterial::getModelEvaluationResult, WireMaterial.EvaluationResult.UNKNOWN)
                .ge(WireMaterial::getCreateTime, LocalDateTime.now().minusHours(hours))
                .orderByDesc(WireMaterial::getCreateTime);
        if (limit != null && limit > 0) {
            if (limit > 1000) limit = 1000;
            wrapper.last("LIMIT " + limit);
        }
        List<WireMaterial> entities = list(wrapper);
        return entities.stream()
                .map(entity -> {
                    WireMaterialDTO dto = new WireMaterialDTO();
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public Result<Boolean> updateEvaluation(Long batchNumber, WireMaterialUpdateDTO dto) {
        log.info("Agent 更新评估结果 — 批次号：{}，结论：{}，置信度：{}",
                batchNumber, dto.getModelEvaluationResult(), dto.getModelConfidence());

        UpdateWrapper<WireMaterial> updateWrapper = Wrappers.update();
        updateWrapper.eq("batch_number", batchNumber);

        WireMaterial entity = new WireMaterial();
        entity.setEvaluationMessage(dto.getEvaluationMessage());
        entity.setModelConfidence(dto.getModelConfidence());

        // String → Enum 转换，容错处理
        if (StringUtils.hasText(dto.getModelEvaluationResult())) {
            try {
                entity.setModelEvaluationResult(
                        WireMaterial.EvaluationResult.valueOf(dto.getModelEvaluationResult()));
            } catch (IllegalArgumentException e) {
                log.warn("无效的评估结果值：{}，跳过更新", dto.getModelEvaluationResult());
                return Result.error("modelEvaluationResult 取值必须为 PASS / FAIL / UNKNOWN");
            }
        }

        boolean updated = update(entity, updateWrapper);
        if (!updated) {
            log.warn("更新评估结果失败，批次号 {} 不存在", batchNumber);
            return Result.error("批次号不存在");
        }
        return Result.success(true);
    }
    @Override
    public Result<Integer> updateEvaluationBatch(List<WireMaterialUpdateDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Result.error("评估列表不能为空");
        }
        int successCount = 0;
        for (WireMaterialUpdateDTO dto : dtoList) {
            UpdateWrapper<WireMaterial> updateWrapper = Wrappers.update();
            updateWrapper.eq("batch_number", dto.getBatchNumber());

            WireMaterial entity = new WireMaterial();
            entity.setEvaluationMessage(dto.getEvaluationMessage());
            entity.setModelConfidence(dto.getModelConfidence());

            if (StringUtils.hasText(dto.getModelEvaluationResult())) {
                try {
                    entity.setModelEvaluationResult(
                            WireMaterial.EvaluationResult.valueOf(dto.getModelEvaluationResult()));
                } catch (IllegalArgumentException e) {
                    log.warn("批次 {} 评估结果值无效：{}，跳过", dto.getBatchNumber(), dto.getModelEvaluationResult());
                    continue;
                }
            }

            boolean updated = update(entity, updateWrapper);
            if (updated) {
                successCount++;
            } else {
                log.warn("批次 {} 不存在，跳过", dto.getBatchNumber());
            }
        }
        log.info("Agent 批量评估完成：{}/{} 条成功", successCount, dtoList.size());
        return Result.success(successCount);
    }
}
