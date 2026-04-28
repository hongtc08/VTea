package com.vtea.controller;

// CỐ TÌNH IMPORT THƯ VIỆN SQL (Vi phạm luật ArchUnit)
import java.sql.Connection;
import java.sql.DriverManager;

public class TestLoiController {

    public void thuKetNoiData() {
        try {
            // Cố tình viết code Database ngay trong giao diện
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/vtea", "root", "1234");
            System.out.println("Kết nối trộm thành công!");
        } catch (Exception e) {
            System.out.println("Lỗi rùi");
        }
    }
}