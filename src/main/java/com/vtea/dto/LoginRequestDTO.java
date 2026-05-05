package com.vtea.dto;

public class LoginRequestDTO {
    private String username;
    private String rawPassword; // Đây là mật khẩu chữ thô (chưa mã hóa) mà người dùng gõ vào

    public LoginRequestDTO(String username, String rawPassword) {
        this.username = username;
        this.rawPassword = rawPassword;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRawPassword() { return rawPassword; }
    public void setRawPassword(String rawPassword) { this.rawPassword = rawPassword; }
}