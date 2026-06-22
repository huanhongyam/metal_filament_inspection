package com.kunpeng.metal_filament_inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kunpeng.metal_filament_inspection.domain.dto.DeviceDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.entity.Device;

import java.util.List;

public interface IDeviceService extends IService<Device> {

    List<DeviceDTO> listPage(Integer current);

    Result<Boolean> saveDevice(Device device);

    Result<Boolean> removeDeviceById(Long deviceId);
}
