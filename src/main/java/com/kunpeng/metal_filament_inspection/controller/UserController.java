package com.kunpeng.metal_filament_inspection.controller;

import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.UserDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.entity.dto.LoginFormDTO;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户信息管理")
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;
    /**
     * 登录功能
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginFormDTO loginForm){
        // 实现登录功能
        return userService.login(loginForm);
    }
    @Operation(summary = "查找当前用户信息")
    @GetMapping("/me")
    public Result<Long> me(){
        // 获取当前登录的用户Id并返回
        Long user = UserHolder.getUser();
        return Result.success(user);
    }
    @Operation(summary = "根据ID查找用户信息")
    @GetMapping("/{id}")
    public Result<User> queryUserById(@PathVariable("id") Long id){
        return Result.success(userService.getById(id));
    }
    @Operation(summary = "根据ID批量删除用户")
    @DeleteMapping()
    public Result<Boolean> deleteUserById(@RequestParam List<Long> ids){
        return Result.success(userService.removeBatchByIds(ids));
    }
    @Operation(summary = "修改用户名")
    @PutMapping("/username")
    public Result<Boolean> updateUsername(@RequestParam("username") String username){
        Long userId = UserHolder.getUser();
        User user = User.builder().userName(username).build();
        return Result.success(userService.updateById(user));
    }
}
