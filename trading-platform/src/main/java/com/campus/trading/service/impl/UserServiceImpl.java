package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.User;
import com.campus.trading.mapper.UserMapper;
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
        // 直接存明文密码
        user.setPassword(user.getPassword());
        user.setRole(0);
        user.setStatus(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setSecurityQuestion(user.getSecurityQuestion());
        user.setSecurityAnswer(user.getSecurityAnswer());
        return userMapper.insert(user) > 0;
    }

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        // 直接比较明文密码
        if (user != null && user.getPassword().equals(password)) {
            user.setPassword(null);
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        users.forEach(user -> user.setPassword(null));
        return users;
    }
}