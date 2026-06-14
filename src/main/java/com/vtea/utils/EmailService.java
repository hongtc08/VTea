package com.vtea.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    private static String EMAIL_USER;
    private static String EMAIL_PASS;

    // Tự động nạp cấu hình email từ file database.properties
    static {
        try (FileInputStream fis = new FileInputStream("database.properties")) {
            Properties prop = new Properties();
            prop.load(fis);
            EMAIL_USER = prop.getProperty("EMAIL_USER");
            EMAIL_PASS = prop.getProperty("EMAIL_PASS");
        } catch (IOException e) {
            System.err.println("[Lỗi] Không tìm thấy file cấu hình email!");
            e.printStackTrace();
        }
    }

    // Hàm tạo mã OTP ngẫu nhiên 6 chữ số
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Đảm bảo luôn ra số có 6 chữ số
        return String.valueOf(otp);
    }

    // Hàm thực hiện gửi email
    public static boolean sendOTPEmail(String toEmail, String otpCode) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Mã hóa TLS

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USER, EMAIL_PASS);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);

            // Tên người gửi hiển thị là "VTea System"
            message.setFrom(new InternetAddress(EMAIL_USER, "VTea System"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã xác thực khôi phục mật khẩu VTea", "UTF-8");

            // Trình bày email bằng HTML
            String htmlContent = "<h3>Xin chào!</h3>"
                    + "<p>Bạn vừa yêu cầu khôi phục mật khẩu cho tài khoản VTea.</p>"
                    + "<p>Mã OTP của bạn là: <b style='font-size: 24px; color: #d32f2f;'>" + otpCode + "</b></p>"
                    + "<p><i>Vui lòng không chia sẻ mã này cho bất kỳ ai. Nếu bạn không yêu cầu thao tác này, hãy phớt lờ email này.</i></p>"
                    + "<br><p>Trân trọng,<br><b>Đội ngũ quản lý VTea</b></p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            System.out.println("Đang gửi email OTP tới " + toEmail + "...");
            Transport.send(message);
            System.out.println("Gửi email thành công!");
            return true;

        } catch (Exception e) {
            System.out.println("Gửi email thất bại: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}