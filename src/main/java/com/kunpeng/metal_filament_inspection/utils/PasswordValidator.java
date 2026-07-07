package com.kunpeng.metal_filament_inspection.utils;

import java.util.regex.Pattern;

/**
 * 密码校验工具类（严格白名单模式）
 * 规则：6~16位，必须包含字母、数字、特殊字符(._?)各至少一个，且只能包含这些字符
 */
public final class PasswordValidator {

    // 允许的字符集白名单：字母 + 数字 + . _ ?
    private static final String ALLOWED_CHARS = "A-Za-z\\d._?";

    // 预编译正则：长度6~16，且仅允许白名单字符，同时满足三大类必须包含
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[._?])[" + ALLOWED_CHARS + "]{6,16}$");

    private PasswordValidator() {
    }

    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 详细校验
     */
    public static String getValidateMessage(String password) {
        if (password == null) {
            return "密码不能为空";
        }
        int len = password.length();
        if (len < 6 || len > 16) {
            return "密码长度必须为 6~16 位";
        }

        // 检查是否包含非法字符（利用白名单取反）
        if (!password.matches("^[" + ALLOWED_CHARS + "]+$")) {
            return "密码只能包含字母、数字以及 . _ ? 这三种特殊字符";
        }

        if (!password.matches(".*[A-Za-z].*")) {
            return "密码必须包含至少一个字母";
        }
        if (!password.matches(".*\\d.*")) {
            return "密码必须包含至少一个数字";
        }
        if (!password.matches(".*[._?].*")) {
            return "密码必须包含 . _ ? 中的至少一个特殊字符";
        }
        return null;
    }
}