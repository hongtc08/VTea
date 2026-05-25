package com.vtea.service;

import com.vtea.dao.UserDAO;
import com.vtea.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class EmployeeService {

    private static final String DEFAULT_PASSWORD = "123456";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_LOCKED = "Locked";

    private final UserDAO userDAO = new UserDAO();

    public static boolean isLocked(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String normalized = status.trim();
        return STATUS_LOCKED.equalsIgnoreCase(normalized)
                || "Inactive".equalsIgnoreCase(normalized)
                || "Disabled".equalsIgnoreCase(normalized);
    }

    public static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_ACTIVE;
        }
        return isLocked(status) ? STATUS_LOCKED : STATUS_ACTIVE;
    }

    public List<User> getAllEmployees() {
        return userDAO.getAllUsers();
    }

    public boolean createEmployee(String username, String fullName, String role, String status) {
        if (username == null || username.isBlank() || fullName == null || fullName.isBlank()) {
            return false;
        }
        if (userDAO.getUserByUsername(username.trim()) != null) {
            return false;
        }

        User user = new User();
        user.setUserName(username.trim());
        user.setFullName(fullName.trim());
        user.setRole(normalizeRole(role));
        user.setStatus(normalizeStatus(status));
        user.setPassWord(BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt()));
        return userDAO.insertUser(user);
    }

    public boolean updateEmployee(User user) {
        if (user == null || user.getUserId() <= 0) {
            return false;
        }
        user.setRole(normalizeRole(user.getRole()));
        user.setStatus(normalizeStatus(user.getStatus()));
        return userDAO.updateUser(user);
    }

    public boolean updateStatus(int userId, String status) {
        return userDAO.updateStatus(userId, normalizeStatus(status));
    }

    /**
     * Đọc trạng thái mới nhất từ DB rồi đảo Active/Locked — tránh dùng object User cũ trên UI.
     */
    public boolean toggleUserLock(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }
        String newStatus = isLocked(user.getStatus()) ? STATUS_ACTIVE : STATUS_LOCKED;
        return userDAO.updateStatus(userId, newStatus);
    }

    public boolean isUserLocked(int userId) {
        User user = userDAO.getUserById(userId);
        return user != null && isLocked(user.getStatus());
    }

    public String getDefaultPasswordHint() {
        return DEFAULT_PASSWORD;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "STAFF";
        }
        if (role.equalsIgnoreCase("ADMIN") || role.contains("Quản")) {
            return "ADMIN";
        }
        return "STAFF";
    }
}
