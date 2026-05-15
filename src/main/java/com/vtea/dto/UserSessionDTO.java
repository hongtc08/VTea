package com.vtea.dto;

public class UserSessionDTO {
    private int id;
    private String username;
    private String fullName;
    private String role;

    public UserSessionDTO(int id, String username, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
}