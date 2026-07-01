package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kunpeng.metal_filament_inspection.domain.dto.DeviceDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.PageDTO;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
    @Autowired
    private IUserService userService;

    public PageDTO<DeviceDTO> listPage(Integer current) {
        Page<Device> page = new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE);
        // 执行分页查询
        IPage<Device> pageResult = page(page, null);
        List<DeviceDTO> dtoList = pageResult.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, DeviceDTO.class))
                .collect(Collectors.toList());
        PageDTO<DeviceDTO> pageDTO = new PageDTO<>();
        pageDTO.setCurrentPage((int) pageResult.getCurrent());
        pageDTO.setPageSize((int) pageResult.getSize());
        pageDTO.setTotal(pageResult.getTotal());
        pageDTO.setRecords(dtoList);
        return pageDTO;
    }
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

    @Override
    public Map<Long, Long> listStart() {
        Long totalCount = query().count();
        Long onCount = query().eq("status", "ON").count();
        return Map.of(totalCount,onCount);
    }
}
