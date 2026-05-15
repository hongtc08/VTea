package com.vtea.main;

import com.vtea.dto.LoginRequestDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.service.AuthService;
import com.vtea.utils.SessionManager;

public class TestLogin {
    public static void main(String[] args) {
        // Gọi bộ não lên
        AuthService authService = new AuthService();

        System.out.println("⏳ Đang kết nối và kiểm tra đăng nhập...");

        try {
            LoginRequestDTO request = new LoginRequestDTO("admin", "admin123");

            // Chạy hàm login
            UserSessionDTO session = authService.login(request);

            // Lưu vào phiên hoạt động
            SessionManager.login(session);

            // In kết quả nếu thành công
            System.out.println("✅ [THÀNH CÔNG] Đăng nhập hợp lệ!");
            System.out.println("👤 Tên nhân viên: " + SessionManager.getCurrentUser().getFullName());
            System.out.println("👑 Quyền hạn: " + SessionManager.getCurrentUser().getRole());
            System.out.println("🔒 Có phải Admin không? " + SessionManager.isAdmin());

        } catch (Exception e) {
            // Nếu sai pass hoặc không tồn tại, nó sẽ nhảy vào đây
            System.out.println("❌ [THẤT BẠI] Lỗi đăng nhập: " + e.getMessage());
        }
    }
}