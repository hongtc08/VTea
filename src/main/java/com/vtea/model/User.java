package com.vtea.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private int userId;
    private String userName;
    private String passWord;
    private String fullName;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    // --- 4 trường dữ liệu mới ---
    private String email;
    private String phone;
    private BigDecimal salary;
    private LocalDate startDate;

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STAFF = "STAFF";

    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_LOCKED = "Locked";

    public User() {
    }

    public User(int userId, String userName, String passWord, String fullName, String role, String status, LocalDateTime createdAt, String email, String phone, BigDecimal salary, LocalDate startDate) {
        this.userId = userId;
        this.userName = userName;
        this.passWord = passWord;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.email = email;
        this.phone = phone;
        this.salary = salary;
        this.startDate = startDate;
    }

    // ================= GETTERS =================

    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getPassWord() { return passWord; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public BigDecimal getSalary() { return salary; }
    public LocalDate getStartDate() { return startDate; }

    // ================= SETTERS =================

    public void setUserId(int userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassWord(String passWord) { this.passWord = passWord; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}