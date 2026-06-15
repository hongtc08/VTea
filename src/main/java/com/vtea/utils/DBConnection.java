package com.vtea.utils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

    private static HikariDataSource dataSource;

    static {

        try (FileInputStream fis =
                     new FileInputStream("database.properties")) {

            Properties prop = new Properties();
            prop.load(fis);

            URL = prop.getProperty("DB_URL");
            USER = prop.getProperty("DB_USER");
            PASSWORD = prop.getProperty("DB_PASS");

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            config.setConnectionTimeout(10000);

            dataSource = new HikariDataSource(config);

            System.out.println("HikariCP initialized");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection()
            throws SQLException {

        long start = System.currentTimeMillis();

        Connection conn = dataSource.getConnection();

        System.out.println(
                "Get connection = "
                        + (System.currentTimeMillis() - start)
                        + " ms");

        return conn;    }
}