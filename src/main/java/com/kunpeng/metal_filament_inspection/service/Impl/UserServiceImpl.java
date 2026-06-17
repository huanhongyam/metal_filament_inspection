package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.UserDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.entity.dto.LoginFormDTO;
import com.kunpeng.metal_filament_inspection.mapper.UserMapper;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.JwtUtil;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.kunpeng.metal_filament_inspection.utils.UserHolder.*;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements IUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<String> login(LoginFormDTO loginForm) {
        String account = loginForm.getAccount();
        String passwd = loginForm.getPasswd();
        User user = query().eq("email",account).one();
        Long id = user.getId();
        String password = user.getPassword();
        String userName = user.getUserName();
        // 校验密码若不一致抛出错误
        if (passwd == null || !passwd.equals(password)){
            return Result.error("密码错误");
        }
        String token = jwtUtil.generateToken(id,userName);
        log.info("用户 [{}] 登录成功", userName);
        return Result.success(token);
    }
}
