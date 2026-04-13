package com.vtea.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    /* =========================================================================================
     * ⚠️ KHU VỰC CẤU HÌNH DATABASE - CHỜ QUYẾT ĐỊNH TỪ TEAM DB
     * =========================================================================================
     * Hiện tại đang setup mặc định cho MySQL chạy trên máy cá nhân (Localhost).
     * Sau khi Database được thiết kế xong, hãy cập nhật lại 3 biến dưới đây theo thống nhất chung.
     */

    // 1. ĐỊA CHỈ DATABASE:
    // - Nếu vẫn dùng máy cá nhân: Giữ nguyên "localhost"
    // - Nếu đưa lên Cloud (Aiven, Clever-Cloud...): Thay "localhost" bằng đường dẫn host được cấp.
    private static final String URL = "jdbc:mysql://localhost:3306/vtea_management?useUnicode=true&characterEncoding=UTF-8";

    // 2. TÀI KHOẢN: Mặc định của MySQL local luôn là "root"
    private static final String USER = "root";

    // 3. MẬT KHẨU (Chú ý khi dùng Git để tránh xung đột file này):
    // - Nhóm xài XAMPP: Thường để trống ""
    // - Nhóm xài Workbench: Điền mật khẩu lúc cài đặt (Ví dụ: "123456", "root")
    // - Nhóm xài Cloud: Điền mật khẩu chung của Cloud
    private static final String PASSWORD = "";

    /* ========================================================================================= */

    // Hàm thực hiện kết nối
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Tải driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Thực hiện kết nối dựa trên 3 biến cấu hình ở trên
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ [Thành công] Đã kết nối tới Database VTea!");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ [Lỗi] Không tìm thấy thư viện MySQL Connector. Hãy kiểm tra lại file pom.xml");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ [Lỗi] Sai thông tin kết nối (URL, User, Pass) hoặc MySQL chưa được bật!");
            e.printStackTrace();
        }
        return conn;
    }
}