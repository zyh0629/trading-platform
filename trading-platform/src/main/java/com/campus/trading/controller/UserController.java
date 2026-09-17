package com.campus.trading.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.User;
import com.campus.trading.mapper.UserMapper;
import com.campus.trading.service.impl.UserService;
import com.campus.trading.utils.JwtUtil;
import com.campus.trading.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功", null);
        }
        return Result.error("注册失败，用户名可能已存在");
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        User user = userService.login(username, password);
        if (user != null) {
            // 生成 JWT Token
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("token", token);

            return Result.success("登录成功", data);
        }
        return Result.error("用户名或密码错误");
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return Result.success(users);
    }

    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        boolean success = userService.updateUser(user);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }

    // ========== 安全问题相关 ==========

    // 获取安全问题（用于忘记密码）
    @GetMapping("/security-question")
    public Result<String> getSecurityQuestion(@RequestParam String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getSecurityQuestion() == null) {
            return Result.error("该用户未设置安全问题");
        }
        return Result.success(user.getSecurityQuestion());
    }

    // 验证安全问题答案并重置密码
    @PostMapping("/reset-by-security")
    public Result<String> resetBySecurity(
            @RequestParam String username,
            @RequestParam String answer,
            @RequestParam String newPassword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getSecurityAnswer() == null) {
            return Result.error("该用户未设置安全问题");
        }
        if (!user.getSecurityAnswer().equalsIgnoreCase(answer)) {
            return Result.error("安全问题答案错误");
        }
        // 直接存明文密码
        user.setPassword(newPassword);
        userMapper.updateById(user);
        return Result.success("密码重置成功", null);
    }

    // 重置密码 - 直接重置为 123456
    @PostMapping("/reset")
    public Result<String> resetPassword(@RequestParam String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword("123456");
        userMapper.updateById(user);
        return Result.success("密码已重置为 123456", null);
    }
}
