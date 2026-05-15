package com.vtea.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // 1. ĐỊA CHỈ DATABASE:
    private static final String URL = "jdbc:mysql://fnb-db-java-se330.g.aivencloud.com:11776/fnb_management?sslMode=REQUIRED";
    // 2. TÀI KHOẢN:
    private static final String USER = "avnadmin";
    // 3. MẬT KHẨU
    private static final String PASSWORD = "";

    /* ========================================================================================= */

    // private static Connection conn = null;

    // Hàm thực hiện kết nối
    public static Connection getConnection() {
        try {
            // Tải driver MySQL (giữ nguyên cho an toàn)
            Class.forName("com.mysql.cj.jdbc.Driver");

            System.out.println("Đang kết nối tới: " + URL);
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ [Thành công] Đã kết nối tới Database VTea!");

            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("❌ [Lỗi] Không tìm thấy thư viện MySQL Connector. Hãy kiểm tra lại file pom.xml");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ [Lỗi] Sai thông tin kết nối (URL, User, Pass) hoặc MySQL chưa được bật!");
            e.printStackTrace();
        }

        return null;
    }

    // Hàm dùng để đóng kết nối khi tắt app (giữ lại cho đúng structure, nhưng không còn cần thiết)
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("🔒 Đã đóng kết nối Database an toàn.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Hàm main để test thử xem code có chạy đúng không
    public static void main(String[] args) {
        Connection conn = getConnection();
        closeConnection(conn);
    }
}
