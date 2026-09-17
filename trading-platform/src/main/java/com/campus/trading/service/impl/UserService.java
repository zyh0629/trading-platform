package com.campus.trading.service.impl;

import com.campus.trading.entity.User;
import java.util.List;

public interface UserService {
    boolean register(User user);
    User login(String username, String password);
    User getUserById(Integer id);
    boolean updateUser(User user);
    List<User> getAllUsers();
}