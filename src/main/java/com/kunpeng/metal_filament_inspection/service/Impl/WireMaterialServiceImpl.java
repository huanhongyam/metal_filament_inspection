package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.mapper.WireMaterialMapper;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class WireMaterialServiceImpl extends ServiceImpl<WireMaterialMapper, WireMaterial> implements IWireMaterialService {
    @Autowired
    private IUserService userService;
    @Override
    public Result<List<WireMaterialDTO>> listPage(Integer current) {
        PageHelper.startPage(current,SystemConstants.DEFAULT_PAGE_SIZE);
        List<WireMaterial> list = list();
        List<WireMaterialDTO> page = list.stream().map(item -> {
            return BeanUtil.copyProperties(item, WireMaterialDTO.class);
        }).toList();
        return Result.success(page);
    }

    @Override
    public Result<Boolean> updateByBatchNumber(WireMaterialDTO wireMaterialDTO, String batchNumber) {
        User user = userService.getById(UserHolder.getUser());
        Integer roleId = user.getRoleId();
        String userName = user.getUserName();
        // 验证管理员权限（不包括Root用户）
        if (roleId != 1) {
            return Result.error("权限不足，仅管理员可操作");
        }
        log.info("管理员{}更新线材信息，批次号：{}", userName, batchNumber);
        UpdateWrapper<WireMaterial> updateWrapper = Wrappers.update();
        updateWrapper.eq("batch_number", batchNumber);
        WireMaterial wireMaterial = BeanUtil.copyProperties(wireMaterialDTO, WireMaterial.class);
        boolean isUpdate = update(wireMaterial, updateWrapper);
        return Result.success(isUpdate);
    }

    @Override
    public Result<Boolean> deleteById(String batchNumber) {
        Long userId = UserHolder.getUser();
        User user = userService.getById(userId);
        Integer roleId = user.getRoleId();
        String userName = user.getUserName();
        // 验证管理员权限（不包括Root用户）
        if (roleId!=1) {
            return Result.error("权限不足，仅管理员可操作");
        }
        log.info("管理员{}删除线材记录，批次号：{}", userName, batchNumber);
        return Result.success(removeById(batchNumber));
    }
}
