package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.domain.dto.PageDTO;
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
    public PageDTO listPage(Integer current) {
        PageHelper.startPage(current, SystemConstants.DEFAULT_PAGE_SIZE);
        List<WireMaterial> list = list();
        Page<WireMaterial> page = (Page<WireMaterial>) list;
        long total = page.getTotal();
        List<WireMaterialDTO> records = list.stream()
                .map(item -> BeanUtil.copyProperties(item, WireMaterialDTO.class))
                .toList();
        PageDTO pageDTO = new PageDTO<>();
        pageDTO.setCurrentPage(current);
        pageDTO.setPageSize(SystemConstants.DEFAULT_PAGE_SIZE);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
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
}
