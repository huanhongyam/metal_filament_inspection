package com.kunpeng.metal_filament_inspection.utils;

import com.kunpeng.metal_filament_inspection.domain.dto.LoginUser;

public class UserHolder {
    private static final ThreadLocal<LoginUser> tl = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser) {
        tl.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        LoginUser loginUser = tl.get();
        if (loginUser == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        return loginUser;
    }
    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static Integer getRoleId() {
        return getLoginUser().getRoleId();
    }

    public static void remove() {
        tl.remove();
    }
}
