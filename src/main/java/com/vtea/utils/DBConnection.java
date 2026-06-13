package com.vtea.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try (FileInputStream fis = new FileInputStream("database.properties")) {
            Properties prop = new Properties();
            prop.load(fis);

            URL = prop.getProperty("DB_URL");
            USER = prop.getProperty("DB_USER");
            PASSWORD = prop.getProperty("DB_PASS");

        } catch (IOException e) {
            System.err.println("❌ [Lỗi nghiêm trọng] Không tìm thấy file database.properties ở thư mục gốc!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("❌ [Lỗi] Không tìm thấy thư viện MySQL Connector.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ [Lỗi] Sai thông tin kết nối hoặc MySQL chưa bật!");
            e.printStackTrace();
        }
        return null;
    }
}