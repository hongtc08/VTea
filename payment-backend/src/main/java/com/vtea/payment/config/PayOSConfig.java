package com.vtea.payment.config;

/**
 * Cấu hình PayOS SDK.
 * Các key được lấy từ biến môi trường
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {

    /**
     * Khởi tạo PayOS client bằng Client ID, API Key và Checksum Key.
     */
    @Bean
    public PayOS payOS() {
        return new PayOS(
                System.getenv("PAYOS_CLIENT_ID"),
                System.getenv("PAYOS_API_KEY"),
                System.getenv("PAYOS_CHECKSUM_KEY")
        );
    }
}