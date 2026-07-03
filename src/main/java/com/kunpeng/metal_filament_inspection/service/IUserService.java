package com.kunpeng.metal_filament_inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.UserRegisterDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.dto.LoginFormDTO;


public interface IUserService extends IService<User> {

    Result<String> login(LoginFormDTO loginForm);

    Result<Boolean> registerUserByEmail(UserRegisterDTO user, String code);

    Result<String> loginWithEmail(String email, String code);
}
