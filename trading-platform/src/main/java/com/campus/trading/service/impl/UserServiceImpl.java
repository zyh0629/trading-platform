package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.User;
import com.campus.trading.mapper.UserMapper;
import com.campus.trading.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean register(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            return false;
        }
        // 密码加密
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        user.setRole(0);
        user.setStatus(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        if (user.getSecurityAnswer() != null && !user.getSecurityAnswer().isBlank()) {
            user.setSecurityAnswer(PasswordUtil.encode(user.getSecurityAnswer()));
        }
        return userMapper.insert(user) > 0;
    }

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        // 用 BCrypt 验证密码
        if (user != null && PasswordUtil.matches(password, user.getPassword())
                && (user.getStatus() == null || user.getStatus() == 0)) {
            sanitize(user);
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            sanitize(user);
        }
        return user;
    }

    @Override
    public boolean updateUser(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            return false;
        }
        existing.setStudentId(user.getStudentId());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setAvatar(user.getAvatar());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(user.getPassword().startsWith("$2")
                    ? user.getPassword() : PasswordUtil.encode(user.getPassword()));
        }
        if (user.getSecurityQuestion() != null) {
            existing.setSecurityQuestion(user.getSecurityQuestion());
        }
        if (user.getSecurityAnswer() != null && !user.getSecurityAnswer().isBlank()) {
            existing.setSecurityAnswer(user.getSecurityAnswer().startsWith("$2")
                    ? user.getSecurityAnswer() : PasswordUtil.encode(user.getSecurityAnswer()));
        }
        existing.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(existing) > 0;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        users.forEach(this::sanitize);
        return users;
    }

    private void sanitize(User user) {
        user.setPassword(null);
        user.setSecurityAnswer(null);
    }
}