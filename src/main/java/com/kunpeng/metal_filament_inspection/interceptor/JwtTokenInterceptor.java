package com.kunpeng.metal_filament_inspection.interceptor;

import cn.hutool.core.util.StrUtil;
import com.kunpeng.metal_filament_inspection.domain.dto.LoginUser;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.utils.JwtUtil;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private IUserService userService;
    private final JwtUtil jwtUtil;

    public JwtTokenInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    /**
     * 校验jwt
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        //1、从请求头中获取令牌
        String token = request.getHeader("authorization");
        String agentToken = request.getHeader("pass4agent");
        //2、校验令牌
        try {
            if (StrUtil.equals(agentToken, SystemConstants.AGENT_TOKEN)){
                log.info("agent调用");
                LoginUser loginUser = new LoginUser();
                loginUser.setUserId(100L);
                loginUser.setRoleId(1);
                UserHolder.setLoginUser(loginUser);
                return true;
            }
            log.info("jwt校验:{}", token);
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            Integer roleId = userService.getById(userId).getRoleId();
            LoginUser loginUser = new LoginUser(userId,roleId);
            log.info("当前用户id:{}", userId);
            log.info("当前用户roleId:{}", roleId);
            UserHolder.setLoginUser(loginUser);
            //3、通过，放行
            return true;
        } catch (Exception e) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.remove();
    }
}
