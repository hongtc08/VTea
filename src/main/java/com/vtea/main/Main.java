package com.vtea.main;

import com.vtea.utils.DBConnection;

public class Main {
    public static void main(String[] args) {
        if (DBConnection.getConnection() != null) {
            System.out.println("Kết nối thông suốt!");
        }
    }
}
