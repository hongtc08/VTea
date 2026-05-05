package com.vtea.utils;

import com.vtea.dto.UserSessionDTO;

public class SessionManager {
    // Biến static này sẽ tồn tại trên RAM từ lúc mở app đến lúc tắt app
    private static UserSessionDTO currentUser;

    // Lưu thông tin khi đăng nhập thành công
    public static void login(UserSessionDTO user) {
        currentUser = user;
    }

    // Lấy thông tin user hiện tại (để in tên lên góc màn hình, in bill...)
    public static UserSessionDTO getCurrentUser() {
        return currentUser;
    }

    // Đăng xuất: Xóa sạch thông tin
    public static void logout() {
        currentUser = null;
    }

    // Hàm tiện ích: Kiểm tra xem người đang dùng có phải Admin không
    public static boolean isAdmin() {
        // So sánh chuỗi an toàn, không phân biệt hoa thường
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}