package com.example.psyaihealer.dto;

import java.util.Set;

/**
 * 兼容 DTO：当前认证接口主要返回 AuthResponse（token/username/roles）。
 * 此类保留用于后续扩展或旧代码兼容。
 */
public class LoginResponse {
    private String token;
    private String username;
    private Set<String> roles;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, Set<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}