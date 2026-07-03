package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.UserRegisterDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.dto.LoginFormDTO;
import com.kunpeng.metal_filament_inspection.mapper.UserMapper;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements IUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private VerificationCodeUtil verificationCodeUtil;
    @Autowired
    private IdWorker idWorker;

    @Override
    public Result<String> login(LoginFormDTO loginForm) {
        String account = loginForm.getAccount();
        String passwd = loginForm.getPasswd();
        User user = query().eq("email",account).one();
        Long id = user.getId();
        String password = user.getPassword();
        String userName = user.getUserName();
        boolean check = BCrypt.checkpw(passwd,password);
        // 校验密码若不一致抛出错误
        if (!check){
            return Result.error("密码错误");
        }
        String token = jwtUtil.generateToken(id,userName);
        log.info("用户 [{}] 登录成功", userName);
        return Result.success(token);
    }

    @Override
    public Result<Boolean> registerUserByEmail(UserRegisterDTO userRegisterDTO, String code) {
        String email =userRegisterDTO.getEmail();

        // 1. 校验验证码
        boolean b = verificationCodeUtil.verifyCode(email, code);
        if (!b) {
            throw new BusinessException("验证码错误或已过期");
        }
        // 2. 校验用户是否已存在
        User existingUser = query().eq("email", email).one();
        if (existingUser != null) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 3. 校验用户名是否已被占用
        User existingByName = query().eq("user_name", userRegisterDTO.getUserName()).one();
        if (existingByName != null) {
            throw new BusinessException("用户名已被占用");
        }
        // 4. 构建新用户
        User user = BeanUtil.copyProperties(userRegisterDTO, User.class);
        String plainPassword = user.getPassword();
        String encodedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        user.setPassword(encodedPassword);
        user.setId(idWorker.generateId(SystemConstants.USER_REGISTER));
        user.setRoleId(0);
        user.setStatus(1);
        user.setAvatarUrl(SystemConstants.QINIU_NORMAL_URL_PREFIX+SystemConstants.USER_REGISTER_NORMAL_AVATAR_URL);
        // 5. 保存用户
        boolean save = save(user);
        log.info("用户注册成功：{}", email);
        return Result.success(save);

    }
}
