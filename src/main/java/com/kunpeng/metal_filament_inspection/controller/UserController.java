package com.kunpeng.metal_filament_inspection.controller;

import cn.hutool.core.util.StrUtil;
import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.UserRegisterDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.dto.LoginFormDTO;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.QiniuUploadUtil;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import com.kunpeng.metal_filament_inspection.utils.VerificationCodeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户信息管理")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private VerificationCodeUtil verificationCodeUtil;
    @Autowired
    private QiniuUploadUtil qiniuUploadUtil;
    /**
     * 登录功能
     */
    @Operation(summary = "登录功能")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginFormDTO loginForm){
        // 实现登录功能
        return userService.login(loginForm);
    }
    /**
     * 发送验证码
     */
    @Operation(summary = "发送验证码")
    @PostMapping("/email")
    public Result<String> login(@RequestParam String email){
        // 实现登录功能
        String s = verificationCodeUtil.sendVerificationCode(email);
        if (StrUtil.isBlank(s)){
            return Result.error("验证码发送失败，请稍后重试");
        }
        return Result.success("发送验证码成功");
    }
    /**
     * 注册用户
     */
    @Operation(summary = "注册用户")
    @PostMapping("/register-user")
    public Result<Boolean> verifyCode(@RequestBody UserRegisterDTO userRegisterDTO,
                                      @RequestParam String code) {
        return userService.registerUserByEmail(userRegisterDTO,code);
    }
    @Operation(summary = "查找当前用户信息")
    @GetMapping("/me")
    public Result<Long> me(){
        // 获取当前登录的用户Id并返回
        Long user = UserHolder.getUserId();
        return Result.success(user);
    }
    @Operation(summary = "根据ID查找用户信息")
    @GetMapping("/{id}")
    public Result<User> queryUserById(@PathVariable("id") Long id){
        return Result.success(userService.getById(id));
    }

    @RequireAdmin
    @Operation(summary = "根据ID批量删除用户")
    @DeleteMapping()
    public Result<Boolean> deleteUserById(@RequestParam List<Long> ids){
        return Result.success(userService.removeBatchByIds(ids));
    }
    @Operation(summary = "修改用户名")
    @PutMapping("/username")
    public Result<Boolean> updateUsername(@RequestParam("username") String username){
        Long userId = UserHolder.getUserId();
        User user = User.builder()
                .userName(username)
                .id(userId)
                .build();
        return Result.success(userService.updateById(user));
    }
}
