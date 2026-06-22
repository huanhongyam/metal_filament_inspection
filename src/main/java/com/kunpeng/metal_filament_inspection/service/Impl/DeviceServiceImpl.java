package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.domain.dto.DeviceDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.entity.Device;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.mapper.DeviceMapper;
import com.kunpeng.metal_filament_inspection.service.IDeviceService;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
    @Autowired
    private IUserService userService;

    @Override
    public List<DeviceDTO> listPage(Integer current) {
        PageHelper.startPage(current,SystemConstants.DEFAULT_PAGE_SIZE);
        List<Device> list = list();
        List<DeviceDTO> page = list.stream().map(item -> {
            return BeanUtil.copyProperties(item, DeviceDTO.class);
        }).toList();
        return page;
    }
    @RequireAdmin
    @Override
    public Result<Boolean> saveDevice(Device device) {
        // 1. 获取当前登录用户ID
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        // 2. 查询用户并判空
        User user = userService.getById(userId);
        if (user == null) {
            log.warn("用户不存在，userId：{}", userId);
            return Result.error("用户不存在");
        }
        // 3. 保存设备并返回结果
        boolean isSuccess = save(device);
        if (isSuccess) {
            log.info("管理员用户{}创建设备成功，设备ID：{}，设备代码：{}",
                    userId, device.getDeviceId(), device.getDeviceCode());
            return Result.success();
        } else {
            log.error("管理员用户{}创建设备失败，设备代码：{}", userId, device.getDeviceCode());
            return Result.error("创建设备失败，请稍后重试");
        }
    }
    @RequireAdmin
    @Override
    public Result<Boolean> removeDeviceById(Long deviceId) {
        // 1. 获取当前登录用户ID
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        // 2. 查询用户并判空
        User user = userService.getById(userId);
        if (user == null) {
            log.warn("用户不存在，userId：{}", userId);
            return Result.error("用户不存在");
        }
        return Result.success(removeById(deviceId));
    }
}
