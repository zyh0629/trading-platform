package com.campus.trading.security;

public class UserPrincipal {
    private final Integer userId;
    private final String username;
    private final Integer role;

    public UserPrincipal(Integer userId, String username, Integer role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Integer getRole() {
        return role;
    }
}
