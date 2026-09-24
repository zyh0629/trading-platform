package com.campus.trading.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    // 加密密码
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 验证密码
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}