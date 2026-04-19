package com.vtea.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String userName;
    private String passWord;
    private String fullName;
    private String role;
    private String status;
    private Timestamp createdAt;

    public User() {

    }

    public User(Timestamp createdAt, String fullName, String passWord, String role, String status, int userId, String userName) {
        this.createdAt = createdAt;
        this.fullName = fullName;
        this.passWord = passWord;
        this.role = role;
        this.status = status;
        this.userId = userId;
        this.userName = userName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPassWord() {
        return passWord;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
